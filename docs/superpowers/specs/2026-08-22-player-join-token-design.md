# Player join tokens

## Purpose

Fibre must let a game reserve a player before the player connects. Features must also be able to attach asynchronous work to that pending join. The game API must not know what the work loads or how a feature uses its result.

`PlayerJoinToken` represents the exclusive right to join one player UUID to one game. The token owns the pending join lifecycle. A preload feature attaches futures to the token, but the token owns waiting, failure, cancellation, and the final player insertion.

## Public usage

A match service issues a token when it assigns a player to a game:

```java
PlayerJoinToken token = game.createPlayerJoinToken(playerId);
```

Issuing the token reserves the UUID across all active games. To move the pending player to another game, cancel the existing token before issuing another token:

```java
gameManager.findPlayerJoinToken(playerId).ifPresent(PlayerJoinToken::cancel);
PlayerJoinToken replacement = otherGame.createPlayerJoinToken(playerId);
```

A preloader attaches asynchronous work during token setup:

```java
registry.registerPlayerPreloader(playerId -> profileService.load(playerId));
```

The server adapter waits during asynchronous pre-login and completes the join after Bukkit creates the `Player`:

```java
token.await();
token.join(player);
```

`join(Player)` also waits. This fallback preserves correctness if no asynchronous pre-login event ran, although it can block the primary server thread.

## Public API

### `Game`

Add this method:

```java
PlayerJoinToken createPlayerJoinToken(UUID playerId);
```

The method throws `IllegalStateException` when the UUID already has a pending token or belongs to an active game. A caller must cancel the pending token or remove the active player before creating a replacement.

Remove `tryAddPlayer(Player)` as the primary join path. All new joins use a token. Keep player removal and active-player queries on `Game`.

### `GameManager`

Add this lookup:

```java
Optional<PlayerJoinToken> findPlayerJoinToken(UUID playerId);
```

`findGame(Player)` continues to return a game only after the token has completed the join. A pending token is not an active game membership.

### `PlayerJoinToken`

Expose this contract:

```java
public interface PlayerJoinToken {
    Game getGame();

    UUID getPlayerId();

    void waitFor(CompletionStage<?> stage);

    void await();

    void join(Player player);

    void cancel();
}
```

`waitFor` attaches a readiness condition. A token accepts conditions until the first call to `await` or `join`. That call seals the token. A later call to `waitFor` throws `IllegalStateException`.

`await` waits for all attached stages. Multiple callers may call `await`. They observe the same result.

`join` verifies that the supplied player has the reserved UUID. It then waits for all attached stages, adds the player to the game, fires `GamePlayerAddEvent`, and releases the pending-token registration. A second call to `join` throws `IllegalStateException`.

`cancel` releases the reservation. Cancellation is idempotent while the token is pending or waiting. Calling `cancel` after a successful join throws `IllegalStateException`; callers remove an active player through `Game#removePlayer`.

If an attached stage fails or is cancelled, the token enters a failed terminal state and releases its reservation. Both `await` and `join` expose the stage failure. The token does not close the game.

### Preloading feature

Keep preloading as a feature API:

```java
@FunctionalInterface
public interface PlayerPreloader {
    CompletableFuture<?> preload(UUID playerId);
}
```

Add this startup registration method:

```java
void registerPlayerPreloader(PlayerPreloader preloader);
```

A preloader receives only the player UUID and returns one non-null future. The preload feature calls every registered preloader and passes each returned future to `PlayerJoinToken#waitFor`. A preloader does not receive or control the token.

Do not expose a generic `PlayerJoinInitializer` or `PlayerJoinProcessor` in `fibre-api`. The preload feature uses a private token-creation hook inside `fibre-plugin`. Other features get their own narrow registration APIs if they need to participate in joining later.

## Ownership

`GameManagerImpl` owns the global UUID reservation index. The index maps each pending UUID to one token and each active UUID to one game. These states are distinct.

`PlayerJoinTokenImpl` owns the state transition from pending to waiting, joined, cancelled, or failed. It also owns the attached stages and the combined completion.

`GameImpl` owns the active player collection and emits add and remove events. The token calls into `GameImpl` only after its wait conditions succeed.

The preload feature owns the registered `PlayerPreloader` list and the private hook that invokes preloaders for a new token. Core game classes do not interpret preload results and do not pass result maps into scopes.

The Bukkit listener is an adapter. It finds a token, calls `await` during `AsyncPlayerPreLoginEvent`, and calls `join` during `PlayerJoinEvent`. It does not inspect token internals or manager implementation classes.

## Token lifecycle

The implementation models these states explicitly:

```text
pending -> waiting -> joined
   |          |
   +----------+-> cancelled
   |          |
   +----------+-> failed
```

The token starts in `pending`. `waitFor` is valid only in this state. The first `await` or `join` changes the state to `waiting` and fixes the set of attached stages.

Successful completion permits one transition to `joined`. Cancellation or failure releases the reservation exactly once. Game closure cancels every pending token owned by that game.

## Main flow

1. The match service calls `game.createPlayerJoinToken(playerId)`.
2. `GameManagerImpl` reserves the UUID and creates the token.
3. The private token hook invokes every registered `PlayerPreloader` with the player UUID.
4. The preload feature passes each returned future to `token.waitFor`.
5. The match service sends the player to the server.
6. `AsyncPlayerPreLoginEvent` finds the token and calls `await` away from the primary thread.
7. `PlayerJoinEvent` finds the same token and calls `join(player)`.
8. The token adds the player to `GameImpl` and leaves the pending-token index.

## Failure and concurrency rules

- Token creation is atomic per UUID. Two games cannot both receive a live token for the same UUID.
- `cancel` and failed stages remove the token only if the reservation index still points to that token.
- A stale token cannot release a newer replacement token.
- `join` rejects a `Player` with a different UUID.
- `join` cannot add a player after the game closes.
- A preload failure rejects that login attempt and logs the failure. It does not close unrelated games or expose a user-facing exception message from an external service.
- The token does not cancel attached stages. A stage may be shared with work outside Fibre.

## Changes to the current implementation

Delete the speculative preload state from `GameImpl` and `GameManagerImpl`. Delete `PlayerPreloadContext`, typed preload-result maps, and the preload-object field added to `GamePlayerAddEvent`.

Restore `PerPlayerContainer` to constructing scopes from `Player` and its parent scope. Features that load data retain their own future results and expose them through their own bindings or services.

Move Bukkit pre-login and join handling behind the public `GameManager` and `PlayerJoinToken` contracts. The listener must not call `FibreImpl#getGameManagerImplementation`.

Use `registerPlayerPreloader`, consistent with the other startup registry methods.

## Tests

Add unit tests for these cases:

- a token waits for every attached stage;
- joining before a stage completes waits for that stage;
- a failed or cancelled stage fails the token and releases the UUID;
- cancellation is idempotent and permits a replacement token;
- a stale cancelled token cannot remove its replacement;
- duplicate token creation fails;
- joining with the wrong UUID fails;
- joining twice fails;
- game closure cancels its pending tokens;
- every registered preloader receives the player UUID;
- the token waits for every future returned by the preloaders;
- a null future fails token setup;
- preload stage results do not enter `GamePlayerAddEvent` or per-player scope inputs.

Run `./gradlew spotlessCheck build :fibre-api:javadoc` after implementation.

## Alternatives

A public `PlayerJoinInitializer` would let plugins intercept every token lifecycle. Preloading does not need that authority, and callback ordering would become part of the public contract.

A manager-owned preparation record would leave waiting and joining rules outside the token. The token would become a handle over manager state rather than the owner of the pending join.

Bukkit token-creation events would make listener priority and exception handling part of token setup. A private callback gives built-in features the same integration point without exposing that ordering.
