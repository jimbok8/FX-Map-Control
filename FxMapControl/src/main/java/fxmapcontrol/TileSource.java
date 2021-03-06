/*
 * FX Map Control - https://github.com/ClemensFischer/FX-Map-Control
 * © 2019 Clemens Fischer
 */
package fxmapcontrol;

import java.util.Locale;

/**
 * Provides the URL of a map tile.
 */
public class TileSource {

    private interface UrlFormatter {

        String getUrl(int x, int y, int z);
    }

    public static final int TILE_SIZE = 256;

    private UrlFormatter urlFormatter;
    private String urlFormat;

    public TileSource() {
    }

    public TileSource(String urlFormat) {
        setUrlFormat(urlFormat);
    }

    public static TileSource valueOf(String urlFormat) {
        return new TileSource(urlFormat);
    }

    public final String getUrlFormat() {
        return urlFormat;
    }

    public final void setUrlFormat(String urlFormat) {
        if (urlFormat == null || urlFormat.isEmpty()) {
            throw new IllegalArgumentException("urlFormat must not be null or empty");
        }

        if (urlFormat.contains("{x}") && urlFormat.contains("{y}") && urlFormat.contains("{z}")) {
            if (urlFormat.contains("{c}")) {
                urlFormatter = (x, y, z) -> getOpenStreetMapUrl(x, y, z);
            } else if (urlFormat.contains("{n}")) {
                urlFormatter = (x, y, z) -> getMapQuestUrl(x, y, z);
            } else {
                urlFormatter = (x, y, z) -> getDefaultUrl(x, y, z);
            }
        } else if (urlFormat.contains("{q}")) {
            urlFormatter = (x, y, z) -> getQuadKeyUrl(x, y, z);

        } else if (urlFormat.contains("{W}") && urlFormat.contains("{S}")
                && urlFormat.contains("{E}") && urlFormat.contains("{N}")) {
            urlFormatter = (x, y, z) -> getBoundingBoxUrl(x, y, z);

        } else if (urlFormat.contains("{w}") && urlFormat.contains("{s}")
                && urlFormat.contains("{e}") && urlFormat.contains("{n}")) {
            urlFormatter = (x, y, z) -> getLatLonBoundingBoxUrl(x, y, z);

        } else {
            throw new IllegalArgumentException("Invalid urlFormat: " + urlFormat);
        }

        this.urlFormat = urlFormat;
    }

    public String getUrl(int x, int y, int zoomLevel) {
        return urlFormatter != null
                ? urlFormatter.getUrl(x, y, zoomLevel)
                : null;
    }

    private String getDefaultUrl(int x, int y, int zoomLevel) {
        return urlFormat
                .replace("{x}", Integer.toString(x))
                .replace("{y}", Integer.toString(y))
                .replace("{z}", Integer.toString(zoomLevel));
    }

    private String getOpenStreetMapUrl(int x, int y, int zoomLevel) {
        int hostIndex = (x + y) % 3;
        return urlFormat
                .replace("{c}", "abc".substring(hostIndex, hostIndex + 1))
                .replace("{x}", Integer.toString(x))
                .replace("{y}", Integer.toString(y))
                .replace("{z}", Integer.toString(zoomLevel));
    }

    private String getMapQuestUrl(int x, int y, int zoomLevel) {
        int hostIndex = (x + y) % 4 + 1;
        return urlFormat
                .replace("{n}", Integer.toString(hostIndex))
                .replace("{x}", Integer.toString(x))
                .replace("{y}", Integer.toString(y))
                .replace("{z}", Integer.toString(zoomLevel));
    }

    private String getQuadKeyUrl(int x, int y, int zoomLevel) {
        if (zoomLevel < 1) {
            return null;
        }

        char[] quadkey = new char[zoomLevel];

        for (int z = zoomLevel - 1; z >= 0; z--, x /= 2, y /= 2) {
            quadkey[z] = (char) ('0' + 2 * (y % 2) + (x % 2));
        }

        return urlFormat
                .replace("{i}", new String(quadkey, zoomLevel - 1, 1))
                .replace("{q}", new String(quadkey));
    }

    private String getBoundingBoxUrl(int x, int y, int zoomLevel) {
        double tileSize = 360d / (1 << zoomLevel); // tile width in degrees
        double west = MapProjection.METERS_PER_DEGREE * (x * tileSize - 180d);
        double east = MapProjection.METERS_PER_DEGREE * ((x + 1) * tileSize - 180d);
        double south = MapProjection.METERS_PER_DEGREE * (180d - (y + 1) * tileSize);
        double north = MapProjection.METERS_PER_DEGREE * (180d - y * tileSize);

        return urlFormat
                .replace("{W}", String.format(Locale.ROOT, "%.1f", west))
                .replace("{S}", String.format(Locale.ROOT, "%.1f", south))
                .replace("{E}", String.format(Locale.ROOT, "%.1f", east))
                .replace("{N}", String.format(Locale.ROOT, "%.1f", north))
                .replace("{X}", Integer.toString(TILE_SIZE))
                .replace("{Y}", Integer.toString(TILE_SIZE));
    }

    private String getLatLonBoundingBoxUrl(int x, int y, int zoomLevel) {
        double tileSize = 360d / (1 << zoomLevel); // tile width in degrees
        double west = x * tileSize - 180d;
        double east = (x + 1) * tileSize - 180d;
        double south = WebMercatorProjection.yToLatitude(180d - (y + 1) * tileSize);
        double north = WebMercatorProjection.yToLatitude(180d - y * tileSize);

        return urlFormat
                .replace("{w}", String.format(Locale.ROOT, "%.6f", west))
                .replace("{s}", String.format(Locale.ROOT, "%.6f", south))
                .replace("{e}", String.format(Locale.ROOT, "%.6f", east))
                .replace("{n}", String.format(Locale.ROOT, "%.6f", north))
                .replace("{X}", Integer.toString(TILE_SIZE))
                .replace("{Y}", Integer.toString(TILE_SIZE));
    }
}
