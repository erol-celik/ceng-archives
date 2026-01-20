import java.io.*;

public class Sorting {

    public static void main(String[] args) throws IOException {

        int[] random1K = readInput("inputs/1K_random_input.txt", 1000);
        int[] random10K = readInput("inputs/10K_random_input.txt", 10000);
        int[] random100K = readInput("inputs/100K_random_input.txt", 100000);

        int[] increasing1K = generateIncreasingArray(1000);
        int[] increasing10K = generateIncreasingArray(10000);
        int[] increasing100K = generateIncreasingArray(100000);

        int[] decreasing1K = generateDecreasingArray(1000);
        int[] decreasing10K = generateDecreasingArray(10000);
        int[] decreasing100K = generateDecreasingArray(100000);


        writeOutputFile("insertionsort_1K_random_output.txt", insertionSort(random1K) );
        writeOutputFile("insertionsort_10K_random_output.txt", insertionSort(random10K) );
        writeOutputFile("insertionsort_100K_random_output.txt", insertionSort(random100K) );

        writeOutputFile("insertionsort_1K_increasing_output.txt", insertionSort(increasing1K) );
        writeOutputFile("insertionsort_10K_increasing_output.txt", insertionSort(increasing10K) );
        writeOutputFile("insertionsort_100K_increasing_output.txt", insertionSort(increasing100K) );

        writeOutputFile("insertionsort_1K_decreasing_output.txt", insertionSort(decreasing1K) );
        writeOutputFile("insertionsort_10K_decreasing_output.txt", insertionSort(decreasing10K) );
        writeOutputFile("insertionsort_100K_decreasing_output.txt", insertionSort(decreasing100K) );

        writeOutputFile("mergesort_1K_random_output.txt", mergeSort(random1K) );
        writeOutputFile("mergesort_10K_random_output.txt", mergeSort(random10K) );
        writeOutputFile("mergesort_100K_random_output.txt", mergeSort(random100K) );

        writeOutputFile("mergesort_1K_increasing_output.txt", mergeSort(increasing1K) );
        writeOutputFile("mergesort_10K_increasing_output.txt", mergeSort(increasing10K) );
        writeOutputFile("mergesort_100K_increasing_output.txt", mergeSort(increasing100K) );

        writeOutputFile("mergesort_1K_decreasing_output.txt", mergeSort(decreasing1K) );
        writeOutputFile("mergesort_10K_decreasing_output.txt", mergeSort(decreasing10K) );
        writeOutputFile("mergesort_100K_decreasing_output.txt", mergeSort(decreasing100K) );

        writeOutputFile("heapsort_1K_random_output.txt", heapSort(random1K) );
        writeOutputFile("heapsort_10K_random_output.txt", heapSort(random10K) );
        writeOutputFile("heapsort_100K_random_output.txt", heapSort(random100K) );

        writeOutputFile("heapsort_1K_increasing_output.txt", heapSort(increasing1K) );
        writeOutputFile("heapsort_10K_increasing_output.txt", heapSort(increasing10K) );
        writeOutputFile("heapsort_100K_increasing_output.txt", heapSort(increasing100K) );

        writeOutputFile("heapsort_1K_decreasing_output.txt", heapSort(decreasing1K) );
        writeOutputFile("heapsort_10K_decreasing_output.txt", heapSort(decreasing10K) );
        writeOutputFile("heapsort_100K_decreasing_output.txt", heapSort(decreasing100K) );

        writeOutputFile("quicksort_1K_random_output.txt", quickSort(random1K) );
        writeOutputFile("quicksort_10K_random_output.txt", quickSort(random10K) );
        writeOutputFile("quicksort_100K_random_output.txt", quickSort(random100K) );

        writeOutputFile("quicksort_1K_increasing_output.txt", quickSort(increasing1K) );
        writeOutputFile("quicksort_10K_increasing_output.txt", quickSort(increasing10K) );
        writeOutputFile("quicksort_100K_increasing_output.txt", quickSort(increasing100K) );

        writeOutputFile("quicksort_1K_decreasing_output.txt", quickSort(decreasing1K) );
        writeOutputFile("quicksort_10K_decreasing_output.txt", quickSort(decreasing10K) );
        writeOutputFile("quicksort_100K_decreasing_output.txt", quickSort(decreasing100K) );
    }

    //sorts array by comparison one by one,measures the time
    public static int[] insertionSort(int[] input) {
        int[] array = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            array[i] = input[i];
        }

        long startTime = System.nanoTime();

        for (int i = 1; i < array.length; i++) {
            int key = array[i];
            int j = i - 1;

            while (j >= 0 && array[j] > key) {
                array[j + 1] = array[j];
                j--;
            }

            array[j + 1] = key;
        }
        long endTime = System.nanoTime();

        double elapsedTimeMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Insertion Sort completed in %.3f ms.\n", elapsedTimeMs);
        return array;
    }
    //calls mergesort and measures time
    public static int[] mergeSort(int[] input) {
        int[] array = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            array[i] = input[i];
        }

        long startTime = System.nanoTime();

        mergeSort(array, 0, array.length - 1);

        long endTime = System.nanoTime();
        double elapsedTimeMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Merge Sort completed in %.3f ms.\n", elapsedTimeMs);        return array;
    }
    //merges two sorted subarray
    public static void merge(int[] array, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        int[] L = new int[n1];
        int[] R = new int[n2];

        for (int i = 0; i < n1; i++) {
            L[i] = array[left + i];
        }

        for (int j = 0; j < n2; j++) {
            R[j] = array[mid + 1 + j];
        }

        int i = 0, j = 0, k = left;

        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                array[k] = L[i];
                i++;
            } else {
                array[k] = R[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            array[k] = L[i];
            i++;
            k++;
        }

        while (j < n2) {
            array[k] = R[j];
            j++;
            k++;
        }
    }
    //recursively divides and sorts the array
    public static void mergeSort(int[] array, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;

            mergeSort(array, left, mid);
            mergeSort(array, mid + 1, right);

            merge(array, left, mid, right);
        }
    }

    //calls buildmaxheap nad maxheapify,measures time
    public static int[] heapSort(int[] input) {
        int size = input.length;
        int[] array = new int[size + 1];

        for (int i = 1; i <= size; i++) {
            array[i] = input[i - 1];
        }

        buildMaxHeap(array, size);

        long startTime = System.nanoTime();
        for (int i = size; i >= 2; i--) {
            int temp = array[1];
            array[1] = array[i];
            array[i] = temp;

            size--;
            maxHeapify(array, 1, size);
        }

        long endTime = System.nanoTime();
        double elapsedTimeMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Heap Sort completed in %.3f ms.\n", elapsedTimeMs);

        //to skip 1. null element of the array
        int[] result = new int[input.length];
        for (int i = 1; i < array.length; i++) {
            result[i - 1] = array[i];
        }

        return result;
    }
    //compares parents and children and swap
    public static void maxHeapify(int[] heap, int parent, int size) {
        int left = 2 * parent;
        int right = 2 * parent + 1;
        int largest = parent;

        if (left <= size && heap[left] > heap[largest]) {
            largest = left;
        }

        if (right <= size && heap[right] > heap[largest]) {
            largest = right;
        }

        if (largest != parent) {
            int temp = heap[parent];
            heap[parent] = heap[largest];
            heap[largest] = temp;

            maxHeapify(heap, largest, size);
        }
    }
    //builds a max heap from an unsorted array.
    public static void buildMaxHeap(int[] heap, int size) {
        for (int i = size / 2; i >= 1; i--) {
            maxHeapify(heap, i, size);
        }
    }

    //calls quickSort and measures time
    public static int[] quickSort(int[] input) {
        int[] array = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            array[i] = input[i];
        }

        long startTime = System.nanoTime();

        quickSort(array, 0, array.length - 1);

        long endTime = System.nanoTime();
        double elapsedTimeMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Quick Sort completed in %.3f ms.\n", elapsedTimeMs);
        return array;
    }
    //recursively calls partition
    public static void quickSort(int[] array, int p, int r) {
        if (p < r) {
            int q = partition(array, p, r);
            quickSort(array, p, q - 1);
            quickSort(array, q + 1, r);
        }
    }
    //selects pivot,divides the array by 2 and sorts by the pivot
    public static int partition(int[] array, int p, int r) {
        int pivot = array[r];
        int i = p - 1;

        for (int j = p; j <= r - 1; j++) {
            if (array[j] <= pivot) {
                i++;
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }

        int temp = array[i + 1];
        array[i + 1] = array[r];
        array[r] = temp;

        return i + 1;
    }

    //reads the input file
    public static int[] readInput(String filePath, int size) {
        int inputs[] = new int[size];
        int index = 0;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null && index < size) {
                inputs[index] = Integer.parseInt(line.trim());
                index++;
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputs;
    }

    //writes the sorted array into txt file by using for each
    public static void writeOutputFile(String fileName, int[] array) throws IOException {
        FileWriter fileWriter = new FileWriter("inputs/" + fileName);
        for (int i = 0; i < array.length; i++) {
            fileWriter.write(array[i] + "\n");
        }
        fileWriter.close();
    }

    //generates increasing array from 1 to size
    public static int[] generateIncreasingArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i + 1;
        }
        return array;
    }

    //generate decreasing array from size to 1
    public static int[] generateDecreasingArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = size - i;
        }
        return array;
    }
}