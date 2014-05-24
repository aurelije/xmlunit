import org.custommonkey.xmlunit.*;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

public class ComparisonTest {

    public static void main(String[] args) {
        URL url1 = ComparisonTest.class.getResource("control.wsdl");
        URL url2 = ComparisonTest.class.getResource("test.wsdl");
        FileReader fr1 = null;
        FileReader fr2 = null;

        try {
            fr1 = new FileReader(url1.getPath());
            fr2 = new FileReader(url2.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        XMLUnit.setCompareUnmatched(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);

        try {

            DetailedDiff detDiff = new DetailedDiff(new Diff(fr1, fr2));
            detDiff.overrideMatchTracker(new MatchTrackerImpl());
            detDiff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
            List<Difference> differences = detDiff.getAllDifferences();

            System.out.println("\nnum of differences: " + differences.size());

            for (Difference difference : differences) {
                if (difference.isRecoverable()) {
                    System.out.println("+++++++++++++++++++++++");
                    System.out.println(difference);
                    System.out.println("+++++++++++++++++++++++");
                } else {
                    System.out.println("- - - - - - - - - - - -");
                    System.out.println(difference);
                    System.out.println("- - - - - - - - - - - -");
                }
            }

            System.out.println("Similar? " + detDiff.similar());
            System.out.println("Identical? " + detDiff.identical());

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class MatchTrackerImpl implements MatchTracker {

    public void matchFound(Difference difference) {
        if (difference != null) {
            NodeDetail controlNode = difference.getControlNodeDetail();
            NodeDetail testNode = difference.getTestNodeDetail();

            String controlNodeValue = printNode(controlNode.getNode());
            String testNodeValue = printNode(testNode.getNode());

            if (controlNodeValue != null) {
                System.out.println("####################");
                System.out.println("Control Node: " + controlNodeValue);
            }
            if (testNodeValue != null) {
                System.out.println("Test Node   : " + testNodeValue);
                System.out.println("####################");
            }
        }
    }

    private static String printNode(Node node) {
        if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
            StringWriter sw = new StringWriter();
            try {
                Transformer t = TransformerFactory.newInstance().newTransformer();
                t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                t.transform(new DOMSource(node), new StreamResult(sw));
            } catch (TransformerException te) {
                System.out.println("nodeToString Transformer Exception");
            }
            return sw.toString();

        }
        return null;
    }
}