package com.arnav.automatedinitialscreening;

import org.apache.log4j.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Arrays;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;

public class Screening {

    public static final Logger logger = Logger.getLogger(Screening.class);

    public static void main(String[] args) {
        String dir = "C:\\Downloads\\Nissan";
        String s = "how";
        logger.info("helloji");
        List<String> files = getAllFiles(dir);
        List<String> filesFound = files.stream().filter(x -> {
            boolean wordMatched = false;
            try {
                wordMatched = FileWordToSearch(x, s);
            } catch(IOException e) {
                e.printStackTrace();
            } catch(TikaException e) {
                e.printStackTrace();
            } catch(SAXException e) {
                e.printStackTrace();
            }
            return wordMatched;
        })
                .collect(Collectors.toList());

        filesFound.stream().forEach(file -> System.out.println(file));
    }

    private static List<String> getAllFiles(String dir) {
        List<String> result = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(Paths.get(dir))) {

            result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected static boolean FileWordToSearch(String fileName, String wordToSearch) throws IOException, TikaException, SAXException {
        String fileType = getFileType(fileName);
        boolean fileMatched = false;
        try {
            switch(fileType) {
                case "application/pdf":
                    fileMatched = matchWordFromPdfFile(fileName, wordToSearch);
                    break;
                case "text/plain":
                    fileMatched = matchWordFromTextFile();
                    break;
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                    fileMatched = matchWordFromDocFile();
                    break;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return fileMatched;
    }

    private static String getFileType(String fileName)  throws IOException {
        File currentFile = new File(fileName);
        Tika tika = new Tika();
        String fileType = tika.detect(currentFile);
        return fileType;
    }

    private static boolean matchWordFromPdfFile(String fileName, String wordToSearch) throws TikaException, IOException,SAXException {
        FileInputStream inputStream = new FileInputStream(new File(fileName));
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext pContext = new ParseContext();
        PDFParser pdfParser = new PDFParser();
        pdfParser.parse(inputStream, handler, metadata, pContext);
        String handlerString = handler.toString();
        boolean wordFound = false;
        wordFound = findWord(handlerString,wordToSearch);
        return wordFound;
    }

    private static boolean matchWordFromTextFile() {
        return false;
    }

    private static boolean matchWordFromDocFile() {
        return false;
    }

    private static boolean findWord(String fileContents, String wordToSearch) {
        List<String> lineContents = Arrays.asList(fileContents.trim().split("\n+\\s*"));
        boolean wordFound = lineContents.stream().anyMatch(s -> s.matches(".*\\b" + wordToSearch + "\\b.*"));
        return wordFound;
    }
}
