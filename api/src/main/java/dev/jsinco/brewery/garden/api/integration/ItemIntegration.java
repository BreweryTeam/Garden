package dev.jsinco.brewery.garden.api.integration;

import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ItemIntegration {

    /**
     * Convert the identifier to an item.
     *
     * @param identifier The identifier string without the item integration namespace
     * @return An optionally present item
     */
    Optional<ItemStack> toBukkit(String identifier);

    /**
     * @return The identifier of this item integration
     */
    String id();

    /**
     * Method that is run in the Garden enable phase.
     */
    void initialize();

    /**
     * @return A completable future that indicates when this integration can have its items validated.
     */
    CompletableFuture<Void> validationReady();

    /**
     * Validate an identifier.
     *
     * @param identifier The identifier to validate without the item integration namespace
     * @return True if valid, otherwise false
     */
    boolean isValid(String identifier);
}
