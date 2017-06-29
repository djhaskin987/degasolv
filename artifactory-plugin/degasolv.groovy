// Plugins Lib Directory
// If your plugin requires any external dependencies, you can place them under
// the ${ARTIFACTORY_HOME}/etc/plugins/lib directory.
// -- https://www.jfrog.com/confluence/display/RTF/User+Plugins
// So, you need to put this there:
// https://mvnrepository.com/artifact/us.bpsm/edn-java/0.5.0
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
    indexFile = new File('index.dsrepo')
    indexFile.withWriter('UTF-8') { writer ->
        writer.write(Printers.printString(
            Printers.defaultPrinterProtocol(),
            repoInfo))
    }
//    println(m.get(newKeyword("x")).toString())
//    println(m.get(newKeyword("y")).toString())
//    println(Parser.END_OF_INPUT.equals(p.nextValue(pbr)))
}

recalculateIndex("./")
