import groovy.io.FileType
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;
import static us.bpsm.edn.Keyword.newKeyword;
import static us.bpsm.edn.parser.Parsers.defaultConfiguration;

def recalculateIndex(dir) {

    findDSCards = ~/\.dscard$/

    baseDir = new File(dir);
    baseDir.eachFileRecurse (FileType.FILES) { file ->
        m = (file =~ findDSCards)
        if (m.find()) {
            println file
        }
    }

    println dir.toString()
    Parseable pbr = Parsers.newParseable("{:x 1, :y 2}")
    Parser p = Parsers.newParser(defaultConfiguration())
    Map<?, ?> m = (Map<?, ?>) p.nextValue(pbr)
    println(m.get(newKeyword("x")).toString())
    println(m.get(newKeyword("y")).toString())
    println(Parser.END_OF_INPUT.equals(p.nextValue(pbr)))
}

recalculateIndex("./")
