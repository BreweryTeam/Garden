package dev.jsinco.brewery.garden.integration.imported.item;

import dev.jsinco.brewery.garden.api.integration.ItemIntegration;
import dev.jsinco.brewery.garden.utility.Logger;

public record IntegrationItemValidator(ItemIntegration itemIntegration, String id, String context) {

    public void validate() {
        itemIntegration.validationReady()
                .thenAccept(this::validate);
    }

    private void validate(Void ignored) {
        if (itemIntegration.isValid(id)) {
            return;
        }
        Logger.logErr("Invalid item definition '%s': unknown id '%s'".formatted(context, id));
    }
}
