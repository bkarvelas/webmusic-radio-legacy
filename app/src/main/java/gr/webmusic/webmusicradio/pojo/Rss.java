package gr.webmusic.webmusicradio.pojo;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Bill Karvelas on 16/3/2015.
 */
@Root(name = "rss") //root of the xml file
public class Rss {

    @Element(required = false)
    private Channel channel;

    public Channel getChannel() {
        return channel;
    }
}

