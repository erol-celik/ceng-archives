#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h> // bool tipi için

#define ORDER 8
#define MAX_NAME_LEN 99

#define LINE_BUFFER_SIZE 1024
#define MAX_RECORDS_FOR_HEAP 2000

int seekCount = 0;
int splitCount = 0;

typedef struct CsvRecord
{
    char id[50];
    char university[MAX_NAME_LEN + 1];
    char department[MAX_NAME_LEN + 1];
    float score;
} CsvRecord;

typedef struct University
{
    char name[MAX_NAME_LEN + 1];
    float score;
    struct University *next;
} University;

typedef struct Node
{
    int isLeaf;
    int numKeys;
    char keys[ORDER - 1][MAX_NAME_LEN + 1];
    union
    {
        struct Node *children[ORDER];
        University *universityLists[ORDER - 1];
    } data;
    struct Node *nextLeaf;
    struct Node *prevLeaf;
} Node;

Node *root = NULL;

typedef struct HeapRecord
{
    CsvRecord record;
} HeapRecord;

typedef struct MergeHeapNode
{
    CsvRecord record;
    int run_idx; // Hangi run dosyasından geldiğini belirtir
} MergeHeapNode;

typedef struct
{
    char department[MAX_NAME_LEN + 1];
    University *uniHead;
} RecordGroup;

//------------------------------ Yardımcı Fonksiyonlar //------------------------------

int csvParser(CsvRecord *record, char *line)
{
    char *score_ptr = strrchr(line, ',');
    if (!score_ptr)
        return 0;
    *score_ptr = '\0';
    (*record).score = atof(score_ptr + 1);

    // ID
    char *token = strtok(line, ",");
    if (!token)
        return 0;
    strncpy((*record).id, token, sizeof((*record).id));
    (*record).id[sizeof((*record).id) - 1] = '\0';

    // University
    token = strtok(NULL, ",");
    if (!token)
        return 0;
    strncpy((*record).university, token, sizeof((*record).university));
    (*record).university[sizeof((*record).university) - 1] = '\0';

    // Department (geri kalan her şey)
    token = strtok(NULL, "");
    if (!token)
        return 0;

    // Baştaki boşluk ve tırnakları temizle
    while (*token == ' ' || *token == '"' || *token == '\t')
        token++;

    // Sondaki tırnak, boşluk, \r, \n temizle
    char *end = token + strlen(token) - 1;
    while (end > token && (*end == ' ' || *end == '"' || *end == '\r' || *end == '\n'))
    {
        *end = '\0';
        end--;
    }

    strncpy((*record).department, token, sizeof((*record).department));
    (*record).department[sizeof((*record).department) - 1] = '\0';

    return 1;
}

// --- Mevcut insertSortedUniversity fonksiyonunu bu kodla değiştirin ---

University *insertSortedUniversity(University *head, const char *name, float score)
{
    University *newNode = malloc(sizeof(University));
    if (!newNode)
    {
        perror("Üniversite düğümü için bellek ayrılamadı");
        return head; // Mevcut listeyi koru
    }
    strncpy((*newNode).name, name, MAX_NAME_LEN);
    (*newNode).name[MAX_NAME_LEN] = '\0';
    (*newNode).score = score;
    (*newNode).next = NULL;

    // Durum 1: Liste boş veya yeni düğüm başa eklenmeli (en yüksek puan).
    if (head == NULL || score > (*head).score)
    {
        (*newNode).next = head;
        return newNode;
    }

    // Durum 2: Araya veya sona eklenecek doğru yeri bul.
    // 'current' işaretçisi, yeni düğümün öncesinde duracak olan düğümdür.
    University *current = head;
    while ((*current).next != NULL && (*(*current).next).score > score)
    {
        current = (*current).next;
    }

    // Yeni düğümü 'current' ile 'current->next' arasına ekle.
    (*newNode).next = (*current).next;
    (*current).next = newNode;

    return head; // Listenin başı değişmediği için 'head'i döndür.
}

// CsvRecord'ları karşılaştırma fonksiyonu (department'a göre artan, score'a göre azalan)
int compareCsvRecords(const void *a, const void *b)
{
    const CsvRecord *recA = (const CsvRecord *)a;
    const CsvRecord *recB = (const CsvRecord *)b;

    int dept_cmp = strcmp((*recA).department, (*recB).department);
    if (dept_cmp != 0)
        return dept_cmp;

    if ((*recA).score > (*recB).score)
        return -1;
    if ((*recA).score < (*recB).score)
        return 1;
    return 0;
}

// -----------------  B+ Ağacı Yardımcı Fonksiyonları  -----------------

Node *createNode(int isLeaf)
{
    Node *newNode = (Node *)malloc(sizeof(Node));
    if (!newNode)
    {
        printf("Memory allocation failed for the node");
    }
    (*newNode).isLeaf = isLeaf;
    (*newNode).numKeys = 0;
    (*newNode).nextLeaf = NULL;
    (*newNode).prevLeaf = NULL;

    if (isLeaf)
    {
        for (int i = 0; i < ORDER - 1; i++)
        {
            (*newNode).data.universityLists[i] = NULL;
        }
    }
    else
    {
        for (int i = 0; i < ORDER; i++)
        {
            (*newNode).data.children[i] = NULL;
        }
    }
    return newNode;
}

Node *findLeaf(Node *node, const char *key)
{
    if (!node)
        return NULL;
    while (!(*node).isLeaf)
    {
        seekCount++;
        int i = 0;
        while (i < (*node).numKeys && strcmp(key, (*node).keys[i]) >= 0)
        {
            i++;
        }
        node = (*node).data.children[i];
        seekCount++;
    }
    return node;
}
char *splitChild(Node *parent, int index, Node *child, Node **newChildPtr)
{
    splitCount++;

    Node *newChild = createNode(child->isLeaf);
    *newChildPtr = newChild;

    int mid = (ORDER - 1) / 2;

    // Doğru numKeys hesabı: orta anahtarı yukarı çıkarıyoruz
    (*newChild).numKeys = (ORDER - 1) - mid;

    for (int i = 0; i < (*newChild).numKeys; i++)
    {
        strncpy((*newChild).keys[i], (*child).keys[mid + i], MAX_NAME_LEN);
        (*newChild).keys[i][MAX_NAME_LEN] = '\0';
    }

    if (!(*child).isLeaf)
    {
        for (int i = 0; i <= (*newChild).numKeys; i++)
        {
            (*newChild).data.children[i] = (*child).data.children[mid + i];
        }
    }
    else
    {
        (*newChild).nextLeaf = (*child).nextLeaf;
        (*newChild).prevLeaf = child;
        if ((*child).nextLeaf)
        {
            (*(*child).nextLeaf).prevLeaf = newChild;
        }
        (*child).nextLeaf = newChild;

        for (int i = 0; i < (*newChild).numKeys; i++)
        {
            (*newChild).data.universityLists[i] = (*child).data.universityLists[mid + i];
            (*child).data.universityLists[mid + i] = NULL;
        }
    }

    (*child).numKeys = mid;

    // promote edilecek key (mid olan)
    char promotedKey[MAX_NAME_LEN + 1];
    strncpy(promotedKey, (*child).keys[mid], MAX_NAME_LEN);
    promotedKey[MAX_NAME_LEN] = '\0';

    // parent'a promote edilen key ve yeni çocuk ekleniyor
    for (int i = (*parent).numKeys; i > index; i--)
    {
        strncpy((*parent).keys[i], (*parent).keys[i - 1], MAX_NAME_LEN);
        (*parent).keys[i][MAX_NAME_LEN] = '\0';
        (*parent).data.children[i + 1] = (*parent).data.children[i];
    }

    strncpy((*parent).keys[index], promotedKey, MAX_NAME_LEN);
    (*parent).keys[index][MAX_NAME_LEN] = '\0';
    (*parent).data.children[index + 1] = newChild;
    (*parent).numKeys++;

    return (*parent).keys[index];
}

char *insertNonFull(Node *node, const char *department, const char *university, float score)
{
    if ((*node).isLeaf)
    {
        // Düzeltilmiş ve Basitleştirilmiş Mantık
        int i = 0;
        // 1. Departmanın mevcut olup olmadığını veya nereye ekleneceğini bul.
        // Döngü sonunda 'i', ya mevcut departmanın ya da yeni eklenecek olanın indeksidir.
        while (i < (*node).numKeys && strcmp(department, (*node).keys[i]) > 0)
        {
            i++;
            seekCount;
        }
        seekCount++;
        // 2. Durum A: Departman zaten mevcut.
        if (i < (*node).numKeys && strcmp(department, (*node).keys[i]) == 0)
        {
            // Sadece üniversiteyi mevcut listeye ekle.
            (*node).data.universityLists[i] =
                insertSortedUniversity((*node).data.universityLists[i], university, score);
            return NULL; // İşlem tamam.
        }
        // 3. Durum B: Departman yeni, eklenmesi gerekiyor.
        else
        {
            // Yeni departman için 'i' indeksinde yer aç.
            // Bunun için 'i' ve sonrasındaki tüm elemanları bir sağa kaydır.
            for (int j = (*node).numKeys; j > i; j--)
            {
                strncpy((*node).keys[j], (*node).keys[j - 1], MAX_NAME_LEN + 1);
                (*node).data.universityLists[j] = (*node).data.universityLists[j - 1];
            }

            // Şimdi 'i' indeksi boş ve güvenli. Yeni departmanı ve verisini ekle.
            strncpy((*node).keys[i], department, MAX_NAME_LEN + 1);
            (*node).data.universityLists[i] = NULL; // Yeni liste için başlangıç.
            (*node).data.universityLists[i] =
                insertSortedUniversity((*node).data.universityLists[i], university, score);

            (*node).numKeys++;
            return NULL; // İşlem tamam.
        }
    }

    else
    {
        int i = (*node).numKeys - 1;
        while (i >= 0 && strcmp(department, (*node).keys[i]) < 0)
        {
            i--;
            seekCount++;
        }
        i++;
        seekCount;

        if ((*(*node).data.children[i]).numKeys == ORDER - 1)
        {
            Node *newChild = NULL;
            char *promotedKey = splitChild(node, i, (*node).data.children[i], &newChild);
            if (strcmp(department, promotedKey) > 0)
            {
                i++;
            }
        }
        return insertNonFull((*node).data.children[i], department, university, score);
    }
}

void insert(RecordGroup *recordList, int recordCount)
{
    int keysPerLeaf = ORDER - 1;
    int numLeaves = (recordCount + keysPerLeaf - 1) / keysPerLeaf;

    Node **leaves = malloc(sizeof(Node *) * numLeaves);
    for (int i = 0; i < numLeaves; i++)
    {
        leaves[i] = createNode(1);
        for (int j = 0; j < keysPerLeaf && (i * keysPerLeaf + j) < recordCount; j++)
        {
            int idx = i * keysPerLeaf + j;
            strncpy((*leaves[i]).keys[j], recordList[idx].department, MAX_NAME_LEN);
            (*leaves[i]).keys[j][MAX_NAME_LEN] = '\0';
            (*leaves[i]).data.universityLists[j] = recordList[idx].uniHead;
            (*leaves[i]).numKeys++;
            seekCount++;
        }
    }

    // Leaf'ler arası bağ
    for (int i = 0; i < numLeaves - 1; i++)
    {
        (*leaves[i]).nextLeaf = leaves[i + 1];
        (*leaves[i + 1]).prevLeaf = leaves[i];
    }

    // Yukarıdan parent node'ları inşa et
    while (numLeaves > 1)
    {
        int group = ORDER;
        int newCount = 0;
        int parentSize = (numLeaves + group - 1) / group;
        splitCount += parentSize - 1;

        Node **newLevel = malloc(sizeof(Node *) * parentSize);

        for (int i = 0; i < numLeaves;)
        {
            Node *parent = createNode(0);
            int childIdx = 0;

            while (childIdx < ORDER && i < numLeaves)
            {
                (*parent).data.children[childIdx] = leaves[i];

                if (childIdx > 0)
                {
                    strncpy((*parent).keys[childIdx - 1], (*leaves[i]).keys[0], MAX_NAME_LEN);
                    (*parent).keys[childIdx - 1][MAX_NAME_LEN] = '\0';
                }

                childIdx++;
                i++;
            }

            (*parent).numKeys = childIdx - 1;
            newLevel[newCount++] = parent;
        }

        free(leaves);
        leaves = newLevel;
        numLeaves = newCount;
    }

    root = leaves[0];
    free(leaves);
}

// --- Heap Fonksiyonları (Replacement Selection için) ---

void minHeapify(HeapRecord arr[], int n, int i)
{
    int smallest = i;
    int l = 2 * i + 1;
    int r = 2 * i + 2;

    if (l < n && compareCsvRecords(&arr[l].record, &arr[smallest].record) < 0)
        smallest = l;

    if (r < n && compareCsvRecords(&arr[r].record, &arr[smallest].record) < 0)
        smallest = r;

    if (smallest != i)
    {
        HeapRecord temp = arr[i];
        arr[i] = arr[smallest];
        arr[smallest] = temp;
        minHeapify(arr, n, smallest);
    }
}

void buildMinHeap(HeapRecord arr[], int n)
{
    for (int i = n / 2 - 1; i >= 0; i--)
        minHeapify(arr, n, i);
}

HeapRecord extractMin(HeapRecord arr[], int *n)
{
    if (*n <= 0)
    {
        fprintf(stderr, "Heap underflow\n");
    }
    HeapRecord root = arr[0];
    arr[0] = arr[*n - 1];
    (*n)--;
    minHeapify(arr, *n, 0);
    return root;
}

void insertHeap(HeapRecord arr[], int *n, HeapRecord item)
{
    if (*n >= MAX_RECORDS_FOR_HEAP)
    {
        fprintf(stderr, "Heap overflow\n");
        return;
    }
    arr[*n] = item;
    (*n)++;
    int i = *n - 1;
    while (i > 0)
    {
        int parent = (i - 1) / 2;
        if (compareCsvRecords(&arr[i].record, &arr[parent].record) < 0)
        {
            HeapRecord temp = arr[i];
            arr[i] = arr[parent];
            arr[parent] = temp;
            i = parent;
        }
        else
            break;
    }
}

// --- Merge Heap Fonksiyonları (Multi-way merge için) ---

void mergeHeapify(MergeHeapNode arr[], int n, int i)
{
    int smallest = i;
    int l = 2 * i + 1;
    int r = 2 * i + 2;

    if (l < n && compareCsvRecords(&arr[l].record, &arr[smallest].record) < 0)
    {
        smallest = l;
    }
    if (r < n && compareCsvRecords(&arr[r].record, &arr[smallest].record) < 0)
    {
        smallest = r;
    }

    if (smallest != i)
    {
        MergeHeapNode temp = arr[i];
        arr[i] = arr[smallest];
        arr[smallest] = temp;
        mergeHeapify(arr, n, smallest);
    }
}

void buildMergeHeap(MergeHeapNode arr[], int n)
{
    for (int i = n / 2 - 1; i >= 0; i--)
    {
        mergeHeapify(arr, n, i);
    }
}

MergeHeapNode extractMergeMin(MergeHeapNode arr[], int *n)
{
    if (*n <= 0)
    {
        fprintf(stderr, "Merge heap underflow\n");
    }
    MergeHeapNode root_node = arr[0];
    arr[0] = arr[*n - 1];
    (*n)--;
    if (*n > 0)
    {
        mergeHeapify(arr, *n, 0);
    }
    return root_node;
}

void writeFinalRecord(FILE *fp, const CsvRecord *record, int rank)
{
    int q1 = strchr((*record).department, ',') != NULL;
    int q2 = strchr((*record).university, ',') != NULL;

    if (q1)
        fprintf(fp, "\"%s\",", (*record).department);
    else
        fprintf(fp, "%s,", (*record).department);

    if (q2)
        fprintf(fp, "\"%s\",", (*record).university);
    else
        fprintf(fp, "%s,", (*record).university);

    fprintf(fp, "%d,", rank);
    fprintf(fp, "%.2f\n", (*record).score);
}

long countRecordsInFile(const char *filename)
{
    FILE *fp = fopen(filename, "r");
    if (!fp)
    {
        printf("Failed to open the file");
        return -1;
    }
    char line[LINE_BUFFER_SIZE];
    long count = 0;
    if (fgets(line, sizeof(line), fp))
    {
        // İlk satır başlık
    }
    while (fgets(line, sizeof(line), fp))
    {
        count++;
    }
    fclose(fp);
    return count;
}

int readRecordFromFile(FILE *fp, CsvRecord *record)
{
    char line[LINE_BUFFER_SIZE];
    if (fgets(line, sizeof(line), fp))
    {
        return csvParser(record, line);
    }
    return 0;
}

unsigned long estimateMemoryUsage(Node *node)
{
    if (!node)
        return 0;

    unsigned long total = sizeof(Node);

    if ((*node).isLeaf)
    {
        for (int i = 0; i < (*node).numKeys; ++i)
        {
            University *curr = (*node).data.universityLists[i];
            while (curr)
            {
                total += sizeof(University);
                curr = (*curr).next;
            }
        }
    }
    else
    {
        for (int i = 0; i <= (*node).numKeys; ++i)
        {
            total += estimateMemoryUsage((*node).data.children[i]);
        }
    }

    return total;
}

void multiwayMergeFromRuns(const char **runFilenames, int numRuns, const char *outputFilename)
{
    // FIX: Declare an array of FILE pointers (FILE**)
    FILE **fps = malloc(numRuns * sizeof(FILE *));
    if (!fps)
    {
        printf("Memory allocation failed (fps)");
    }

    for (int i = 0; i < numRuns; i++)
    {
        // FIX: Now fps[i] is a FILE*, which is correct
        fps[i] = fopen(runFilenames[i], "r");
        if (!fps[i])
        { // This check is now valid
            fprintf(stderr, "Run dosyası açılamadı: %s\n", runFilenames[i]);
        }
        char dummy[LINE_BUFFER_SIZE];
        fgets(dummy, sizeof(dummy), fps[i]); // This call is now valid
    }

    FILE *out = fopen(outputFilename, "w");
    if (!out)
    {
        printf("Failed to write the final file");
    }

    fprintf(out, "id,university,department,score\n");

    MergeHeapNode *heap = malloc(numRuns * sizeof(MergeHeapNode));
    int heapSize = 0;

    for (int i = 0; i < numRuns; i++)
    {
        CsvRecord rec;
        if (readRecordFromFile(fps[i], &rec))
        { // This call is now valid
            heap[heapSize].record = rec;
            heap[heapSize].run_idx = i;
            heapSize++;
        }
    }

    buildMergeHeap(heap, heapSize);

    while (heapSize > 0)
    {
        MergeHeapNode min = extractMergeMin(heap, &heapSize);
        CsvRecord *r = &min.record;
        fprintf(out, "%s,%s,%s,%.2f\n", (*r).id, (*r).university, (*r).department, (*r).score);

        CsvRecord nextRec;
        if (readRecordFromFile(fps[min.run_idx], &nextRec))
        {
            heap[heapSize].record = nextRec;
            heap[heapSize].run_idx = min.run_idx;
            heapSize++;

            int i = heapSize - 1;
            while (i > 0)
            {
                int parent = (i - 1) / 2;
                if (compareCsvRecords(&heap[i].record, &heap[parent].record) < 0)
                {
                    MergeHeapNode tmp = heap[i];
                    heap[i] = heap[parent];
                    heap[parent] = tmp;
                    i = parent;
                }
                else
                    break;
            }
        }
    }

    fclose(out);
    for (int i = 0; i < numRuns; i++)
    {
        if (fps[i])
        {
            fclose(fps[i]); // This call is now valid
        }
    }
    free(fps);
    free(heap);
}

// --- Ağaç Görüntüleme ve Bellek Yönetimi Fonksiyonları ---

void printTreeRecursive(Node *node, int level)
{
    if (!node)
        return;

    for (int k = 0; k < level; k++)
        printf("  ");
    printf("Level %d [", level);
    for (int i = 0; i < (*node).numKeys; i++)
    {
        printf("'%s'", (*node).keys[i]);
        if (i < (*node).numKeys - 1)
            printf(" | ");
    }
    printf("] (%s)\n", (*node).isLeaf ? "Leaf" : "Internal");

    if (!(*node).isLeaf)
    {
        for (int i = 0; i <= (*node).numKeys; i++)
        {
            printTreeRecursive((*node).data.children[i], level + 1);
        }
    }
    else
    {
        for (int i = 0; i < (*node).numKeys; i++)
        {
            for (int k = 0; k < level; k++)
                printf("  ");
            printf("  Department: '%s' Universities:\n", (*node).keys[i]);
            University *uni = (*node).data.universityLists[i];
            int rank = 1;
            while (uni)
            {
                for (int k = 0; k < level; k++)
                    printf("  ");
                printf("    #%d %s (%.2f)\n", rank++, (*uni).name, (*uni).score);
                uni = (*uni).next;
            }
        }
        for (int k = 0; k < level; k++)
            printf("  ");
        printf("-------------------\n");
    }
}

void printTree()
{
    if (!root)
    {
        printf("Tree is empty.\n");
        return;
    }
    printTreeRecursive(root, 0);
}

void freeTree(Node *node)
{
    if (!node)
        return;

    if ((*node).isLeaf)
    {
        for (int i = 0; i < (*node).numKeys; i++)
        {
            University *currentUni = (*node).data.universityLists[i];
            while (currentUni)
            {
                University *temp = currentUni;
                currentUni = (*currentUni).next;
                free(temp);
            }
        }
    }
    else
    {
        for (int i = 0; i <= (*node).numKeys; i++)
        {
            freeTree((*node).data.children[i]);
        }
    }
    free(node);
}

void searchTree(const char *department)
{
    if (!root)
    {
        printf("The tree is empty. Search cannot be performed.\n");
        return;
    }

    Node *leaf = findLeaf(root, department);
    if (!leaf)
    {
        printf("Search failed: No corresponding leaf found for '%s'.\n", department);
        return;
    }

    int found_index = -1;
    for (int i = 0; i < (*leaf).numKeys; i++)
    {
        seekCount++;
        if (strcmp(department, (*leaf).keys[i]) == 0)
        {
            found_index = i;
            break;
        }
    }

    if (found_index != -1)
    {
        printf("\n--- Universities in the '%s' Department ---\n", department);
        University *uni = (*leaf).data.universityLists[found_index];
        if (!uni)
        {
            printf("There are no university records for this department yet.\n");
            return;
        }
        int rank = 1;
        while (uni)
        {
            printf("#%d %s (%.2f)\n", rank++, (*uni).name, (*uni).score);
            uni = (*uni).next;
        }
        printf("------------------------------------------\n");
    }
    else
    {
        printf("The '%s' department was not found in the tree.\n", department);
    }
}

void searchByRank(const char *department, int rank)
{
    if (!root)
    {
        printf("The tree is empty. Search cannot be performed.\n");
        return;
    }

    Node *leaf = findLeaf(root, department);
    seekCount++;

    int found_index = -1;
    for (int i = 0; i < (*leaf).numKeys; i++)
    {
        if (strcmp(department, (*leaf).keys[i]) == 0)
        {
            found_index = i;
            break;
        }
    }

    if (found_index != -1)
    {
        University *uni = (*leaf).data.universityLists[found_index];
        int current = 1;
        while (uni && current < rank)
        {
            uni = (*uni).next;
            current++;
            seekCount++;
        }
        if (uni && current == rank)
        {
            printf("\nThe %dᵗʰ university in the '%s' department:\n", rank, department);
            printf("%s (%.2f)\n", (*uni).name, (*uni).score);
        }
        else
        {
            printf("Error: The %dᵗʰ university in the '%s' department could not be found.\n", rank, department);
        }
    }
    else
    {
        printf("Department '%s' was not found in the tree.\n", department);
    }
}
// Replacement Selection algoritması ile Bulk Load işlemi
void bulkLoad_ReplacementSelection(const char *inputFilename)
{
    splitCount =0;
    FILE *inputFile = fopen(inputFilename, "r");
    if (!inputFile)
    {
        printf("Failed to open CSV file");
        return;
    }

    char line[LINE_BUFFER_SIZE];
    // Başlık satırını oku (varsa)
    if (!fgets(line, sizeof(line), inputFile))
    {
        fprintf(stderr, "CSV is empty or the header could not be read.\n");
        fclose(inputFile);
        return;
    }

    // Heap veri yapısı
    HeapRecord heap[MAX_RECORDS_FOR_HEAP];
    int heapSize = 0;

    // Başlangıçta heap'i doldur
    while (heapSize < MAX_RECORDS_FOR_HEAP && fgets(line, sizeof(line), inputFile))
    {
        CsvRecord rec;
        if (csvParser(&rec, line))
        {
            HeapRecord h = {rec};
            insertHeap(heap, &heapSize, h);
        }
    }

    int runCount = 0;
    float lastScore = -1.0f;
    char lastDept[MAX_NAME_LEN + 1] = "";

    // Heap boşalana kadar run üret
    while (heapSize > 0)
    {
        char runName[64];
        sprintf(runName, "temp_run_%d.csv", runCount++);
        FILE *run = fopen(runName, "w");
        if (!run)
        {
            printf("Run dosyası");
            continue;
        }

        // Run dosyasına başlık yaz
        fprintf(run, "id,university,department,score\n");

        int recordsInRun = 0;
        CsvRecord nextRunBuffer[MAX_RECORDS_FOR_HEAP];
        int nextRunSize = 0;

        // Heap'ten sırayla kayıtları al
        while (heapSize > 0)
        {
            HeapRecord minRec = extractMin(heap, &heapSize);

            // Sıralama kurallarına göre bu kayıt bir sonraki run'a mı gitmeli?
            int deptCmp = strcmp(minRec.record.department, lastDept);
            int outOfOrd = (recordsInRun > 0) &&
                           (deptCmp < 0 || (deptCmp == 0 && minRec.record.score > lastScore));

            if (outOfOrd)
            {
                if (nextRunSize < MAX_RECORDS_FOR_HEAP)
                {
                    nextRunBuffer[nextRunSize++] = minRec.record;
                }
                continue;
            }

            // Kayıt uygun, dosyaya yaz
            fprintf(run, "%s,%s,%s,%.2f\n",
                    minRec.record.id,
                    minRec.record.university,
                    minRec.record.department,
                    minRec.record.score);

            // Son değerleri güncelle
            strncpy(lastDept, minRec.record.department, MAX_NAME_LEN);
            lastDept[MAX_NAME_LEN] = '\0';
            lastScore = minRec.record.score;
            recordsInRun++;

            // Yeni satır oku ve sıraya göre heap veya buffer'a ekle
            if (fgets(line, sizeof(line), inputFile))
            {
                CsvRecord newRec;
                if (csvParser(&newRec, line))
                {
                    int deptCmp2 = strcmp(newRec.department, lastDept);
                    int outOfOrder = (recordsInRun > 0) &&
                                     (deptCmp2 < 0 || (deptCmp2 == 0 && newRec.score > lastScore));

                    if (outOfOrder)
                    {
                        if (nextRunSize < MAX_RECORDS_FOR_HEAP)
                        {
                            nextRunBuffer[nextRunSize++] = newRec;
                        }
                    }
                    else
                    {
                        HeapRecord h = {newRec};
                        insertHeap(heap, &heapSize, h);
                    }
                }
            }
        }

        fclose(run);

        // Bir sonraki run için buffer'dan heap'e geri yükle
        for (int i = 0; i < nextRunSize; ++i)
        {
            HeapRecord h = {nextRunBuffer[i]};
            insertHeap(heap, &heapSize, h);
        }

        // Sıralama kontrolü için son değerleri sıfırla
        lastDept[0] = '\0';
        lastScore = -1.0f;
    }

    fclose(inputFile);

    // Run dosyalarının adlarını hazırla
    char **runFiles = malloc(runCount * sizeof(char *));
    for (int i = 0; i < runCount; ++i)
    {
        runFiles[i] = malloc(64);
        sprintf(runFiles[i], "temp_run_%d.csv", i);
    }

    // Merge işlemini başlat
    multiwayMergeFromRuns((const char **)runFiles, runCount, "final_sorted_records.csv");

    // Belleği serbest bırak
    for (int i = 0; i < runCount; ++i)
    {
        free(runFiles[i]);
    }
    free(runFiles);

    // Final dosyasını kontrol et
    FILE *fp = fopen("final_sorted_records.csv", "r");
    if (!fp)
    {
        printf("final_sorted_records.csv açılamadı.\n");
        return;
    }

    // Başlık satırını atla
    if (!fgets(line, sizeof(line), fp))
    {
        fclose(fp);
        return;
    }
}

int treeHeight(Node *node)
{
    if (node == NULL)
    {
        return 0; // Boş ağacın yüksekliği 0'dır
    }

    int height = 0;
    Node *current = node;

    // Kökten başlayarak yaprak düğüme in
    while (!(*current).isLeaf)
    {
        height++;
        current = (*current).data.children[0]; // İlk çocuğu takip et (her yol aynı yükseklikte olmalı)
    }
    // Yaprak düğüme ulaştığımızda, yaprak düğümü de sayıma dahil et
    height++;
    return height;
}

RecordGroup *buildRecordGroupsFromFile(const char *filename, int *outCount)
{
    FILE *fp = fopen(filename, "r");
    if (!fp)
    {
        printf("Failed to open file: %s\n", filename);
        return NULL;
    }

    char line[LINE_BUFFER_SIZE];
    fgets(line, sizeof(line), fp); // Başlığı atla

    RecordGroup *recordList = malloc(sizeof(RecordGroup) * MAX_RECORDS_FOR_HEAP); // Gerekirse arttır
    int count = 0;

    while (fgets(line, sizeof(line), fp))
    {
        CsvRecord rec;
        if (!csvParser(&rec, line))
            continue;

        if (count > 0 && strcmp(rec.department, recordList[count - 1].department) == 0)
        {
            recordList[count - 1].uniHead =
                insertSortedUniversity(recordList[count - 1].uniHead, rec.university, rec.score);
        }
        else
        {
            strncpy(recordList[count].department, rec.department, MAX_NAME_LEN);
            recordList[count].department[MAX_NAME_LEN] = '\0';
            recordList[count].uniHead = insertSortedUniversity(NULL, rec.university, rec.score);
            count++;
        }
    }

    fclose(fp);
    *outCount = count;
    return recordList;
}

// ----------------  MAIN ----------------
int main(int argc, char *argv[])
{
    if (argc < 3 || argc > 4)
    {
        printf("Please choose a loading option:\n");
        printf("1 - Sequential Insertion\n");
        printf("2 - Bulk Loading (with external merge sort)\n");
        printf("Usage: %s <1:sequential, 2:bulk> <csv_file> [department_to_search]\n", argv[0]);
        return 1;
    }

    int mode = atoi(argv[1]); // 1 = sequential, 2 = bulk
    char *filename = argv[2];
    char *search_department_arg = (argc == 4) ? argv[3] : NULL;

    long total_records = countRecordsInFile(filename);
    if (total_records > 0)
        printf("The input file contains approximately %ld records (excluding the header).\n", total_records);

    // ---------------- SEQUENTIAL LOADING ----------------
    if (mode == 1)
    {
        printf("--- Sequential loading started ---\n");
        FILE *fp = fopen(filename, "r");
        if (!fp)
        {
            printf("CSV file could not be opened.\n");
            return 1;
        }

        char line[LINE_BUFFER_SIZE];
        if (!fgets(line, sizeof(line), fp))
        {
            fprintf(stderr, "Error: CSV file is empty or header could not be read.\n");
            fclose(fp);
            return 1;
        }

        int record_count = 0;
        while (fgets(line, sizeof(line), fp))
        {
            CsvRecord rec;
            if (csvParser(&rec, line))
            {
                if (!root)
                    root = createNode(1); // create first leaf
                if (root->numKeys == ORDER - 1)
                {
                    Node *newRoot = createNode(0);
                    newRoot->data.children[0] = root;
                    Node *dummy = NULL;
                    splitChild(newRoot, 0, root, &dummy);
                    root = newRoot;
                }
                insertNonFull(root, rec.department, rec.university, rec.score);
                record_count++;
            }
            else
            {
                fprintf(stderr, "Warning: Malformed line skipped: %s\n", line);
            }
        }

        fclose(fp);
        printf("Sequential loading completed.\n");
        printf("A total of %d records were processed.\n", record_count);
    }

    // ---------------- BULK LOADING ----------------
    else if (mode == 2)
    {
        printf("--- Bulk loading started (Replacement Selection) ---\n");
        bulkLoad_ReplacementSelection(filename);

        int recordCount = 0;
        RecordGroup *recordList = buildRecordGroupsFromFile("final_sorted_records.csv", &recordCount);
        insert(recordList, recordCount);
        free(recordList);

        printf("Bulk loading completed.\n");
    }
    else
    {
        printf("Invalid option. Enter 1 for sequential load or 2 for bulk load.\n");
        return 1;
    }

    // ---------------- METRICS OUTPUT ----------------
    printf("\nMetrics Output:\n");
    printf("Number of splits: %d\n", splitCount);
    unsigned long memBytes = estimateMemoryUsage(root);
    printf("Memory usage: %.2f KB\n", memBytes / 1024.0); // for memBytes / (1024.0 * 1024.0)
    printf("Tree height: %d\n", treeHeight(root));
    printf("Average seek time: %.3f ms\n", (double)seekCount);  // çünkü 1 seek = 1 ms
    seekCount = 0;

    // ---------------- SEARCH INTERFACE ----------------
    if (search_department_arg)
    {
        printf("\n--- Command Line Search Test ---\n");
        searchTree(search_department_arg);
    }
    else
    {
        printf("\nPlease enter the department name to search:\n");
        char dept_input[MAX_NAME_LEN + 1];
        int rank_input;
        while (1)
        {
            printf(">> ");
            if (!fgets(dept_input, sizeof(dept_input), stdin)) break;
            dept_input[strcspn(dept_input, "\n")] = 0;
            if (strcmp(dept_input, "q") == 0) break;

            printf("Please enter the university rank in that department:\n>> ");
            char rank_line[16];
            if (!fgets(rank_line, sizeof(rank_line), stdin)) break;
            rank_input = atoi(rank_line);
            if (rank_input <= 0)
            {
                printf("Invalid rank number!\n");
                continue;
            }

            searchByRank(dept_input, rank_input);
            printf("Estimated seek time: %.4f sec\n", (double)seekCount * 0.001);
            seekCount = 0;
        }
    }

    // ---------------- CLEANUP ----------------
    freeTree(root);
    root = NULL;
    return 0;
}
