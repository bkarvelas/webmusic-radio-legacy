package gr.webmusic.webmusicradio.pojo;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Bill Karvelas on 16/3/2015.
 */
@Root(name = "item", strict = false)
public class Item {

    private String thumbnailUrl;

    @Element(name = "title", required = false)
    public static String title;

    @Element(data = true, name = "description", required = false)
    public String description;

    public String getDescription() {
        return description;
    }

    public static String getTitle() {
        return title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
