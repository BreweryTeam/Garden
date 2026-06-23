package dev.jsinco.brewery.garden.api.integration;

public interface IntegrationRegistry {

    /**
     * Register a new item integration to garden.
     *
     * @param itemIntegration The item integration to register
     */
    void registerItemIntegration(ItemIntegration itemIntegration);
}
