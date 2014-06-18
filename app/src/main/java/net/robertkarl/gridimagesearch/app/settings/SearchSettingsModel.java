package net.robertkarl.gridimagesearch.app.settings;

import java.io.Serializable;

public class SearchSettingsModel implements Serializable {

    public String imageSize;
    public String imageColor;
    public String imageType;
    public String siteFilter;

    public SearchSettingsModel() {
        imageSize = "all";
        imageColor= "all";
        imageType = "all";
        siteFilter = "";
    }
}

