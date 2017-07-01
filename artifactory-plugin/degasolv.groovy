// Plugins Lib Directory
// If your plugin requires any external dependencies, you can place them under
// the ${ARTIFACTORY_HOME}/etc/plugins/lib directory.
// -- https://www.jfrog.com/confluence/display/RTF/User+Plugins
// So, you need to put these there:
// https://mvnrepository.com/artifact/us.bpsm/edn-java/0.5.0
// https://mvnrepository.com/artifact/org.apache.commons/commons-lang3/3.6
// https://mvnrepository.com/artifact/org.codehaus.plexus/plexus-utils/3.0.24
// https://mvnrepository.com/artifact/org.apache.maven/maven-artifact/3.5.0
import us.bpsm.edn.parser.Parseable
import us.bpsm.edn.parser.Parser
import us.bpsm.edn.parser.Parsers
import us.bpsm.edn.TaggedValue
import static us.bpsm.edn.Keyword.newKeyword
import static us.bpsm.edn.parser.Parsers.defaultConfiguration;
import java.util.Map
import java.util.List
import java.util.HashMap
import java.util.ArrayList
import groovy.io.FileType
import us.bpsm.edn.printer.Printers

def recalculateIndex(dir) {

    Map<String, List<TaggedValue>> repoInfo =
        new HashMap<String, List<TaggedValue>>();

    findDSCards = ~/\.dscard$/
    idKeyword = newKeyword("id")

    Parser p = Parsers.newParser(defaultConfiguration())
    baseDir = new File(dir);
    baseDir.eachFileRecurse (FileType.FILES) { file ->
        m = (file =~ findDSCards)
        if (m.find()) {
            String cardContents = file.getText('UTF-8')
            println cardContents
            Parseable pbr = Parsers.newParseable(cardContents)
            t = p.nextValue(pbr)
            Map<?, ?> m = t.getValue()
            String id = m.get(idKeyword)
            if (!repoInfo.containsKey(id)) {
                repoInfo.put(id, new ArrayList<TaggedValue>())
            }
            lst = repoInfo.get(id)
            lst.add(t)
        }
    }
    // for each here that sorts each list using magics

    indexFile = new File('index.dsrepo')
    indexFile.withWriter('UTF-8') { writer ->
        writer.write(Printers.printString(
            Printers.defaultPrinterProtocol(),
            repoInfo))
    }
}

recalculateIndex("./")
