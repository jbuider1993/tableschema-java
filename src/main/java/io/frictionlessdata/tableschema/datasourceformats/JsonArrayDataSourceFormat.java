package io.frictionlessdata.tableschema.datasourceformats;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.json.CDL;
import org.json.JSONArray;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 *
 */
public class JsonArrayDataSourceFormat extends AbstractDataSourceFormat {

    public JsonArrayDataSourceFormat(String dataSource){
        super(dataSource);
    }

    public JsonArrayDataSourceFormat (InputStream inStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(inputStreamReader);

        String content = br.lines().collect(Collectors.joining("\n"));
        content = DataSourceFormat.trimBOM(content);
        br.close();
        inputStreamReader.close();
        this.dataSource = new JSONArray(content);
    }


    @Override
    public void write(File outputFile) throws Exception {
        try (Writer out = new BufferedWriter(new FileWriter(outputFile))) {
            out.write((String)dataSource);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean hasReliableHeaders() {
        return false;
    }

    /**
     * Retrieve the CSV Parser.
     * The parser works record wise. It is not possible to go back, once a
     * record has been parsed from the input stream. Because of this, CSVParser
     * needs to be recreated every time:
     * https://commons.apache.org/proper/commons-csv/apidocs/index.html?org/apache/commons/csv/CSVParser.html
     *
     * @return a CSVParser instance that works CSV data generated by converting the JSON-array data to CSV
     * @throws Exception thrown if the parser throws an exception
     */
    @Override
    CSVParser getCSVParser() throws Exception{
        String dataSourceString;
        if (dataSource instanceof String){
            dataSourceString = (String)dataSource;

        } else if(dataSource instanceof JSONArray){
            dataSourceString = dataSource.toString();

        } else{
            throw new Exception("Data source is of invalid type.");
        }
        String dataCsv = CDL.toString(new JSONArray(dataSourceString));
        Reader sr = new StringReader(dataCsv);
        // Get the parser.
        return CSVParser.parse(sr, DataSourceFormat.getDefaultCsvFormat());
    }
}
