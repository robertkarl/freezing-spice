package net.robertkarl.gridimagesearch.app;

import net.robertkarl.gridimagesearch.app.settings.SearchSettingsModel;

/**
 * Describes a search, encapsulating everything needed to re-run the image query.
 */
public class SearchHistoryModel {
    public SearchSettingsModel searchSettings;
    public String query;
}
