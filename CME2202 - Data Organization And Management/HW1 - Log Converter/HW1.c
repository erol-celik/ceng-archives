#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <json-c/json.h>
#include <libxml/parser.h>
#include <libxml/tree.h>
#include <libxml/xmlschemastypes.h>

struct DeviceLog
{
    char device_id[8];
    char timestamp[20];
    double temperature;
    int humidity;
    char status[32];
    char location[31];
    char alert_level[10];
    int battery;
    char firmware_ver[10];
    int event_code;
};
typedef struct DeviceLog DeviceLog;

// copy data yaparken daha kolay null değer atamak için kullanılıyor
typedef enum
{
    TYPE_STRING,
    TYPE_INT,
    TYPE_DOUBLE,
} FieldDataType;

// json için gerekli static değişkenler
char dataFileName[100];
int keyStart;
int keyEnd;
char order[10];

// setupParams.json dosyasindan gerekli parametreleri okur
void read_json_parameters()
{
    struct json_object *root = json_object_from_file("setupParams.json");
    if (root == NULL)
    {
        printf("setupParams.json opening failed.\n");
    }

    struct json_object *j_dataFileName;
    struct json_object *j_keyStart;
    struct json_object *j_keyEnd;
    struct json_object *j_order;

    if (json_object_object_get_ex(root, "dataFileName", &j_dataFileName))
    {
        if (json_object_get_string(j_dataFileName) == NULL)
        {
            printf("dataFileName is NULL.\n");
        }
        strcpy(dataFileName, json_object_get_string(j_dataFileName));
    }
    else
    {
        printf("dataFileName is missing in JSON file.\n");
    }

    if (json_object_object_get_ex(root, "keyStart", &j_keyStart))
    {
        keyStart = json_object_get_int(j_keyStart);
    }
    else
    {
        printf("keyStart is missing in JSON file.\n");
    }

    if (json_object_object_get_ex(root, "keyEnd", &j_keyEnd))
    {
        keyEnd = json_object_get_int(j_keyEnd);
    }
    else
    {
        printf("keyEnd is missing in JSON file.\n");
    }

    if (json_object_object_get_ex(root, "order", &j_order))
    {
        if (json_object_get_string(j_order) == NULL)
        {
            printf("order is NULL.\n");
        }
        strcpy(order, json_object_get_string(j_order));
    }
    else
    {
        printf("order is missing in JSON file.\n");
    }

    printf("\nLoaded JSON parameters:\n");
    printf("dataFileName: %s\n", dataFileName);
    printf("keyStart: %d\n", keyStart);
    printf("keyEnd: %d\n", keyEnd);
    printf("order: %s\n", order);

    json_object_put(root); //
}

// logdatadan binary verileri okuyup memoryde arraya yazıyor
DeviceLog *read_binary_file(int *record_count)
{
    FILE *fp = fopen(dataFileName, "rb");
    if (fp == NULL)
    {
        printf("Can't open binary file.\n");
    }

    fseek(fp, 0, SEEK_END);     // dosyanın sonuna gider
    long file_size = ftell(fp); // dosyanın byte uzunlugu
    rewind(fp);                 // dosynanın başına dönülür

    int count = file_size / sizeof(DeviceLog); // record (csvdeki line) satırını hesaplar
    *record_count = count;                     // ve maine gönderir

    // record sayısına göre memory allocate eder
    DeviceLog *records = (DeviceLog *)malloc(count * sizeof(DeviceLog));
    if (records == NULL)
    {
        printf("Memory allocation failed.\n");
        fclose(fp);
    }

    fread(records, sizeof(DeviceLog), count, fp);
    fclose(fp);
    printf("Data is written to memory.\n");

    return records;
}

// recordları device_id'lere gore siralar
void sort_records(DeviceLog *records, int record_count)
{
    int k, m;
    int cmp_len = keyEnd - keyStart + 1;

    if (strcmp(order, "ASC") == 0)
    {
        for (k = 0; k < record_count - 1; k++)
        {
            for (m = 0; m < record_count - k - 1; m++)
            {
                if (strncmp(records[m].device_id + keyStart, records[m + 1].device_id + keyStart, cmp_len) > 0)
                {
                    DeviceLog temp = records[m];
                    records[m] = records[m + 1];
                    records[m + 1] = temp;
                }
            }
        }
    }
    else if (strcmp(order, "DESC") == 0)
    {
        for (k = 0; k < record_count - 1; k++)
        {
            for (m = 0; m < record_count - k - 1; m++)
            {
                if (strncmp(records[m].device_id + keyStart, records[m + 1].device_id + keyStart, cmp_len) < 0)
                {
                    DeviceLog temp = records[m];
                    records[m] = records[m + 1];
                    records[m + 1] = temp;
                }
            }
        }
    }
}

// field tipine göre datayı kopyalar, gerekirse baştaki ve sondaki boşlukları temizler
void copy_data(void *dest, const char *src, size_t size, FieldDataType type)
{
    // get a copy to trim
    char temp[256];
    strncpy(temp, src, sizeof(temp) - 1);
    temp[sizeof(temp) - 1] = '\0';

    // trim
    char *start = temp;
    char *end;

    while (isspace((unsigned char)*start))
        start++;

    if (*start == 0)
    {
        temp[0] = '\0';
    }
    else
    {
        end = start + strlen(start) - 1;
        while (end > start && isspace((unsigned char)*end))
            end--;
        *(end + 1) = '\0';
    }
    // end trim

    // field null değilse değerini yazar
    if (strlen(start) == 0)
    {
        if (type == TYPE_STRING)
        {
            strncpy((char *)dest, "", size - 1);
            ((char *)dest)[size - 1] = '\0';
        }
        else if (type == TYPE_INT)
        {
            *(int *)dest = 0;
        }
        else if (type == TYPE_DOUBLE)
        {
            *(double *)dest = 0.0;
        }
        return;
    }

    // field null ise "" veya 0 yazar
    if (type == TYPE_STRING)
    {
        strncpy((char *)dest, start, size - 1);
        ((char *)dest)[size - 1] = '\0';
    }
    else if (type == TYPE_INT)
    {
        *(int *)dest = atoi(start);
    }
    else if (type == TYPE_DOUBLE)
    {
        *(double *)dest = atof(start);
    }
}

// read data from csv and write turn into binary file.
void read_csv_and_write_binary(char sep_char, char *line_end)
{
    FILE *fp;
    DeviceLog data;

    fp = fopen("smartlogs.csv", "r");
    if (fp == NULL)
    {
        printf("CSV is NULL!\n");
    }

    FILE *log_file = fopen("logdata.dat", "wb");
    if (log_file == NULL)
    {
        printf("Bin is NULL!\n");
        fclose(fp);
    }

    char line[512];
    char fields[20][100];

    fgets(line, sizeof(line), fp); // skip 1. line

    while (fgets(line, sizeof(line), fp))
    {

        int i = 0;
        while (line[i] != '\0')
        {
            if (strncmp(&line[i], line_end, strlen(line_end)) == 0)
            {
                line[i] = '\0'; // end of line
                break;
            }
            i++;
        }

        // seperate line into fields
        int field_idx = 0, char_idx = 0;
        i = 0;
        while (line[i] != '\0' && field_idx < 20)
        {
            if (line[i] == sep_char)
            {
                fields[field_idx][char_idx] = '\0';
                field_idx++;
                char_idx = 0;
            }
            else
            {
                fields[field_idx][char_idx++] = line[i];
            }
            i++;
        }
        fields[field_idx][char_idx] = '\0';

        // copy each field by its data type
        copy_data(data.device_id, fields[0], sizeof(data.device_id), TYPE_STRING);
        copy_data(data.timestamp, fields[1], sizeof(data.timestamp), TYPE_STRING);
        copy_data(&data.temperature, fields[2], sizeof(double), TYPE_DOUBLE);
        copy_data(&data.humidity, fields[3], sizeof(int), TYPE_INT);
        copy_data(data.status, fields[4], sizeof(data.status), TYPE_STRING);
        copy_data(data.location, fields[5], sizeof(data.location), TYPE_STRING);
        copy_data(data.alert_level, fields[6], sizeof(data.alert_level), TYPE_STRING);
        copy_data(&data.battery, fields[7], sizeof(int), TYPE_INT);
        copy_data(data.firmware_ver, fields[8], sizeof(data.firmware_ver), TYPE_STRING);
        copy_data(&data.event_code, fields[9], sizeof(int), TYPE_INT);

        fwrite(&data, sizeof(DeviceLog), 1, log_file);
    }

    fclose(fp);
    fclose(log_file);
    printf("Data is written in binary file.\n");
}


// event_code'dan big endian hex, little endian hex ve little endian decimal hesaplar
void process_event_code(unsigned short event_code, char *hexBig, char *hexLittle, unsigned short *decimalFromLittle)
{
    unsigned char highBytes = (event_code >> 8) & 0xFF;
    unsigned char lowBytes = event_code & 0xFF;

    sprintf(hexBig, "%02X %02X", highBytes, lowBytes);    // Big Endian Hex
    sprintf(hexLittle, "%02X %02X", lowBytes, highBytes); // Little Endian Hex
    *decimalFromLittle = (lowBytes << 8) | highBytes;     // Little Endian Decimal
}

// read binary from logdata.dat and writo into xml file
void read_binary_and_write_xml()
{
    FILE *log_file = fopen("logdata.dat", "rb");
    if (log_file == NULL)
    {
        printf("Binary log file opening failed");
    }

    FILE *xml_file = fopen("2022510164_2022510012.xml", "w");
    if (xml_file == NULL)
    {
        printf("XML file opening failed");
        fclose(log_file);
    }

    fprintf(xml_file, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    fprintf(xml_file, "<LogData>\n");

    DeviceLog data;
    while (fread(&data, sizeof(DeviceLog), 1, log_file))
    {
        char hexBig[10];
        char hexLittle[10];
        unsigned short decimalFromLittle;

        process_event_code(data.event_code, hexBig, hexLittle, &decimalFromLittle);

        fprintf(xml_file, "  <Device>\n");
        fprintf(xml_file, "    <DeviceID>%s</DeviceID>\n", data.device_id);
        fprintf(xml_file, "    <Timestamp>%s</Timestamp>\n", data.timestamp);
        fprintf(xml_file, "    <Temperature>%.2f</Temperature>\n", data.temperature);

        // Humidity kontrolü
        if (data.humidity == 0)
        {
            fprintf(xml_file, "    <Humidity>%d</Humidity>\n", -1);
        }
        else
        {
            fprintf(xml_file, "    <Humidity>%d</Humidity>\n", data.humidity);
        }

        fprintf(xml_file, "    <Status>%s</Status>\n", data.status);
        fprintf(xml_file, "    <Location>%s</Location>\n", data.location);
        fprintf(xml_file, "    <AlertLevel>%s</AlertLevel>\n", data.alert_level);

        // Battery kontrolü
        if (data.battery == 0)
        {
            fprintf(xml_file, "    <Battery>%d</Battery>\n", -1);
        }
        else
        {
            fprintf(xml_file, "    <Battery>%d</Battery>\n", data.battery);
        }

        fprintf(xml_file, "    <FirmwareVersion>%s</FirmwareVersion>\n", data.firmware_ver);
        fprintf(xml_file, "    <EventCodeDecimal>%d</EventCodeDecimal>\n", data.event_code);
        fprintf(xml_file, "    <EventCodeHexBigEndian>%s</EventCodeHexBigEndian>\n", hexBig);
        fprintf(xml_file, "    <EventCodeHexLittleEndian>%s</EventCodeHexLittleEndian>\n", hexLittle);
        fprintf(xml_file, "    <EventCodeDecimalFromLittleEndian>%d</EventCodeDecimalFromLittleEndian>\n", decimalFromLittle);
        fprintf(xml_file, "  </Device>\n");
    }

    fprintf(xml_file, "</LogData>\n");

    fclose(log_file);
    fclose(xml_file);
    printf("\nData has been written in Xml file.\n");
}

void validate()
{

    xmlDocPtr doc;
    xmlSchemaPtr schema = NULL;
    xmlSchemaParserCtxtPtr ctxt;

    char *XMLFileName = "2022510164_2022510012.xml"; // write your xml file here
    char *XSDFileName = "2022510164_2022510012.xsd"; // write your xsd file here

    xmlLineNumbersDefault(1);                   // set line numbers, 0> no substitution, 1>substitution
    ctxt = xmlSchemaNewParserCtxt(XSDFileName); // create an xml schemas parse context
    schema = xmlSchemaParse(ctxt);              // parse a schema definition resource and build an internal XML schema
    xmlSchemaFreeParserCtxt(ctxt);              // free the resources associated to the schema parser context

    doc = xmlReadFile(XMLFileName, NULL, 0); // parse an XML file
    if (doc == NULL)
    {
        fprintf(stderr, "Could not parse %s\n", XMLFileName);
    }
    else
    {
        xmlSchemaValidCtxtPtr ctxt; // structure xmlSchemaValidCtxt, not public by API
        int ret;

        ctxt = xmlSchemaNewValidCtxt(schema);  // create an xml schemas validation context
        ret = xmlSchemaValidateDoc(ctxt, doc); // validate a document tree in memory
        if (ret == 0)                          // validated
        {
            printf("%s validates\n", XMLFileName);
        }
        else if (ret > 0) // positive error code number
        {
            printf("%s fails to validate\n", XMLFileName);
        }
        else // internal or API error
        {
            printf("%s validation generated an internal error\n", XMLFileName);
        }
        xmlSchemaFreeValidCtxt(ctxt); // free the resources associated to the schema validation context
        xmlFreeDoc(doc);
    }
    // free the resource
    if (schema != NULL)
        xmlSchemaFree(schema); // deallocate a schema structure

    xmlSchemaCleanupTypes(); // cleanup the default xml schemas types library
    xmlCleanupParser();      // cleans memory allocated by the library itself
    xmlMemoryDump();         // memory dump
}

int main(int argc, char *argv[]) {
    if (argc < 2) {
        fprintf(stderr, "Error: Missing arguments. Use -h for help.\n");
        return 1;
    }

  // Help flag kontrolü
for (int i = 1; i < argc; i++) {
    if (strcmp(argv[i], "-h") == 0) {
        printf("Command Line Usage:\n");
        printf("# Usage:\n");
        printf("./2022510164_2022510012 <input_file> <output_file> <conversion_type> -separator <1|2|3> -opsys <1|2|3> [-h]\n\n");
        printf("# Arguments:\n");
        printf("<input_file> = Source file to read from\n");
        printf("<output_file> = Target file to write to (or the XSD file for validation)\n");
        printf("<conversion_type>:\n");
        printf("  1 ~ CSV to Binary\n");
        printf("  2 ~ Binary to XML (reads binary file name from setupParams.json)\n");
        printf("  3 ~ XML validation with XSD\n");
        printf("-separator <P1> = Required field separator (1=comma, 2=tab, 3=semicolon)\n");
        printf("-opsys <P2> = Required line ending type (1=windows, 2=linux, 3=macos)\n\n");
        printf("# Optional Flags:\n");
        printf("-h    Display help message and exit\n\n");
        printf("# Examples:\n");
        printf("./2022510164_2022510012 smartlog.csv logdata.dat 1 -separator 1 -opsys 2\n");
        printf("./2022510164_2022510012 logdata.dat smartlogs.xml 2 -separator 1 -opsys 2\n");
        printf("./2022510164_2022510012 smartlogs.xml smartlogs.xsd 3 -separator 1 -opsys 2\n");
        printf("./2022510164_2022510012 -h\n");
        return 0;
    }
}

    // Minimum argüman kontrolü (en az 5 olmalı: ./deviceTool input output conversion -separator X -opsys Y)
    if (argc < 6) {
        fprintf(stderr, "Error: Missing required arguments. Use -h for help.\n");
        return 1;
    }

    char *input_file = argv[1];
    char *output_file = argv[2];
    int conversion_type = atoi(argv[3]);
    char separatorType = ','; // Varsayılan: Virgül
    char *opsys = "\n";       // Varsayılan: Linux (line ending)

    // Argümanları işle
    for (int i = 4; i < argc; i++) {
        if (strcmp(argv[i], "-separator") == 0 && i + 1 < argc) {
            int sep_value = atoi(argv[i + 1]);
            if (sep_value == 1) separatorType = ',';
            else if (sep_value == 2) separatorType = '\t';
            else if (sep_value == 3) separatorType = ';';
            i++;
        }
        else if (strcmp(argv[i], "-opsys") == 0 && i + 1 < argc) {
            int os_value = atoi(argv[i + 1]);
            if (os_value == 1) opsys = "\r\n";
            else if (os_value == 2) opsys = "\n";
            else if (os_value == 3) opsys = "\r";
            i++;
        }
    }

    // Dönüşüm tipine göre işlem yap
    switch (conversion_type) {
        case 1: // CSV to Binary
            read_csv_and_write_binary(separatorType, opsys);
            printf("CSV to Binary conversion completed.\n");
            break;
            
        case 2: // Binary to XML
            read_json_parameters();
            int record_count = 0;
            DeviceLog *records = read_binary_file(&record_count);
            sort_records(records, record_count);
            read_binary_and_write_xml();
            printf("Binary to XML conversion completed.\n");
            break;
            
        case 3: // XML validation
            validate();
            break;
            
        default:
            fprintf(stderr, "Error: Invalid conversion type. Use 1, 2, or 3.\n");
            return 1;
    }

    return 0;
}