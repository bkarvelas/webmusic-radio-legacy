package gr.webmusic.webmusicradio.model;

/**
 * Created by Bill Karvelas on 2/3/2015.
 */
public class WebsiteArticleList {
    private String thumbnailUrl;
    private String title;

    public WebsiteArticleList() {
    }

    public WebsiteArticleList(String name, String thumbnailUrl) {
        this.title = name;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

}
