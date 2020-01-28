package gr.webmusic.webmusicradio.pojo;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
public class Channel{

    @ElementList(entry="item")
    private List<Item> item;

    public Channel(){};

    public List<Item> getMatches(){
        return item;
    }
}
