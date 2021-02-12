import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

public class OSMProcessor {

    XMLStreamReader xmlReader;
    private static Logger logger = LogManager.getLogger(OSMProcessor.class);

    public OSMProcessor(XMLStreamReader xmlReader) {
        this.xmlReader = xmlReader;
    }

    //Заполняет входные мапы в соответствии с заданием
    public void ProcessOSM(Map<String, Integer> userEditsCount, Map<String, Integer> tagNodeCount) throws XMLStreamException {

        xmlReader.nextTag();
        while (! xmlReader.getLocalName().equals("node"))
            xmlReader.nextTag();                               //skip all elements before nodes

        int n = 0;
        int last_logged_node_num = 0;
        while(xmlReader.getLocalName().equals("node")){
            ProcessNode(xmlReader, userEditsCount, tagNodeCount);
            n++;
            if (n == (last_logged_node_num + 1000000)){
                last_logged_node_num = n;
                logger.info("Done with " + last_logged_node_num + " node");
            }
        }
        logger.info("Finish on " + last_logged_node_num + " node");

    }


    //Обрабатывает ноду, в результате оставляя курсор на следующей ноде, или после последней ноды.
    void ProcessNode(XMLStreamReader xmlReader, Map<String, Integer> userEditsCount, Map<String, Integer> tagNodeCount) throws XMLStreamException {

        String user = xmlReader.getAttributeValue(null, "user");

        if(userEditsCount.containsKey(user)){
            userEditsCount.replace(user, userEditsCount.get(user) + 1);
        }
        else {
            userEditsCount.put(user, 1);
        }

        xmlReader.nextTag();

        while (xmlReader.getLocalName().equals("tag")){
            ProcessTag(xmlReader, tagNodeCount);
        }
        xmlReader.nextTag();
    }

    //Обрабатывает тэг, перемешая курсор после закрытия данного тэга.
    void ProcessTag(XMLStreamReader xmlReader, Map<String, Integer> tagNodeCount) throws XMLStreamException {
        String tagKey = xmlReader.getAttributeValue(null, "k");

        //Согласно вики по OpenStreetMap https://wiki.openstreetmap.org/wiki/Node у одной Node не может быть двух тегов с одинаковыми ключами
        if(tagNodeCount.containsKey(tagKey)){
            tagNodeCount.replace(tagKey, tagNodeCount.get(tagKey) + 1);
        }
        else {
            tagNodeCount.put(tagKey, 1);
        }

        //Переход на закрытие тэга, и на следующий после него тэг
        xmlReader.nextTag();
        xmlReader.nextTag();
    }

}
