import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {

        HashedDictionary<String, Media> archive = new HashedDictionary<>();
        System.out.println("\nWelcome to Media Archive!");
        menu(archive);

        //fastestInsertingSettings(archive);
    }


    static void menu(HashedDictionary<String, Media> archive) {
        Scanner scannerMenu = new Scanner(System.in);
        boolean menuFlag = true;
        int operation;
        while (menuFlag) {
            System.out.println("\n1 > Insert Medias");
            System.out.println("2 > Run 1000 Search Test *");
            System.out.println("3 > Search a Media with IMDbId *");
            System.out.println("4 > Search Streamed Medias by Country *");
            System.out.println("5 > Top 10 Media *");
            System.out.println("6 > List of Medias that are streaming on all five platforms *");
            System.out.println("\n0 > Reinsert Medias");
            System.out.println("\n9 > Exit");
            System.out.println("\nArchive must be inserted for * Operations\n\n>Please Choose an Operation");

            operation = scannerMenu.nextInt();
            System.out.println();

            if (operation == 1) getMediaFromFile(archive);
            else if (operation == 2) searchTest_1000(archive);
            else if (operation == 3) searchByImdbID(archive);
            else if (operation == 4) searchMediasByCountry(archive);
            else if (operation == 5) highestRatedMedias(archive);
            else if (operation == 6) allFivePlatforms(archive);
            else if (operation == 0) {
                archive.clear();
                getMediaFromFile(archive);
                menu(archive);
            } else if (operation == 9) menuFlag = false;
            else System.out.println("Enter a Valid Operaiton!");
            System.out.println("------------------------------------------------------------");
        }
    }

    static void settings(HashedDictionary<String, Media> archive) {
        Scanner scanner = new Scanner(System.in);

        //set load factor
        System.out.println(">> Enter a Load Factor Between 0 and 1 <<");
        while (true) {
            try {
                double newLoadFactor = Double.parseDouble(scanner.nextLine());
                if (newLoadFactor > 0 && newLoadFactor < 1) {
                    archive.setLoadFactor(newLoadFactor);
                    System.out.println(">Load factor is: " + newLoadFactor + " now\n");
                    break;
                } else {
                    System.out.println("Invalid input. Please enter a value between 0 and 1.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric value between 0 and 1.");
            }
        }

        //set hash funciton
        System.out.println(">>> Choose Your Hash Function >(1 or 2)< <<<\n>> 1 For Simple Summation Function(SSF)\n>> 2 For Polynomial Accumulation Function(PAF)");
        String choice = scanner.nextLine();
        if (choice.equals("1")) {
            archive.getHashCalculator().setHashMethod(true); // SAF selected
            System.out.println(">SSF selected.");
        } else if (choice.equals("2")) {
            archive.getHashCalculator().setHashMethod(false);  // PAF selected
            System.out.println(">PAF selected.");
        } else {
            System.out.println("Invalid input! Please choose 1 or 2.");
        }
        System.out.println();


        //set collision handling type
        System.out.println(">>> Choose Collision Handling Type (1 or 2) <<<\n>> 1 for Linear Probing (LP)\n>> 2 for Double Hashing (DH)");
        String choice2 = scanner.nextLine();
        if (choice2.equals("1")) {
            archive.setCollisionHandling(true);  // Lineer Probing (LP) selected.
            System.out.println(">Linear Probing (LP) selected.");
        } else if (choice2.equals("2")) {
            archive.setCollisionHandling(false);  // Double Hashing (DH) selected.
            System.out.println(">Double Hashing (DH) selected.");
        } else {
            System.out.println("Invalid input. Collision Handling set as default.");
        }
        System.out.println();


        //scanner.close();
    }

    static void getMediaFromFile(HashedDictionary<String, Media> archive) {
        //gets medias from file and adds in a hashedDictionary
        //prints number of lines,collisions,duration and hashedDictionary's size
        settings(archive);
        String filePath = "C:/Users/Erol/OneDrive/Masaüstü/movies_dataset.csv";
        long startTime = System.currentTimeMillis();  //start time
        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {
            String str;
            str = br.readLine();
            while ((str = br.readLine()) != null) {
                int lastIndex = 0;
                int firstIndex = 0;
                String[] parts = new String[10];
                for (int i = 0; i < 10; i++) {
                    if (i != 0) {
                        lastIndex++;
                        firstIndex = lastIndex;
                    }
                    if (((i == 1 || i == 3 || i == 9) && str.charAt(lastIndex) == '"')) {//for possible " " and , characters in those parts .
                        if (i == 1) {
                            lastIndex++;
                            firstIndex = lastIndex;
                            while (true) {
                                if (str.charAt(lastIndex) == '"' && str.charAt(lastIndex + 1) == ',') break;
                                lastIndex++;
                            }
                            parts[i] = str.substring(firstIndex, lastIndex);
                            lastIndex++;
                        } else {
                            lastIndex++;
                            firstIndex = lastIndex;
                            while (str.charAt(lastIndex) != '"') {
                                lastIndex++;
                            }
                            parts[i] = str.substring(firstIndex, lastIndex);
                            lastIndex++;
                        }
                    } else {
                        if (i == 9) {
                            parts[i] = str.substring(firstIndex, str.length());
                        } else {
                            while (str.charAt(lastIndex) != ',') {
                                lastIndex++;
                            }
                            parts[i] = str.substring(firstIndex, lastIndex);
                        }
                    }
                }

                String url = parts[0];
                if (url.length() == 0) url = "No available link for this platform";

                String name = parts[1];

                String type = parts[2];

                List<String> genres = new List<>();
                genres.arrayToList(parts[3].trim().split(","));

                int releaseYear;
                if (parts[4] != "") releaseYear = Integer.parseInt(parts[4]);//checks release year if its null
                else releaseYear = 0;

                String imdbId = parts[5];

                double avgRating;
                if (parts[6] != "") avgRating = Double.parseDouble(parts[6]); //checks rating if its null
                else avgRating = 0.0;

                int voteNumber;
                if (parts[7] != "") voteNumber = Integer.parseInt(parts[7]);//checks number of vote if its null
                else voteNumber = 0;

                String platforms = parts[8];

                String[] countries;
                if (parts[9].length() < 4) countries = parts[9].split("", 1);
                else countries = parts[9].replace(" ", "").split(",");

                if (archive.contains(imdbId)) {
                    archive.getValue(imdbId).addPlatformAndCountry(platforms, countries, url);
                } else {
                    Media media = new Media(url, imdbId, name, type, genres, releaseYear, avgRating, voteNumber, platforms, countries);
                    archive.add(media.getImdbId(), media);
                }
                //System.out.println(lineCount);
                lineCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();  // end time
        long duration = endTime - startTime;
        System.out.println(lineCount + " media read in " + duration + " milliseconds with " + archive.getCollisionCount() +
                " collisions.\nArchive size is: " + archive.getSize() + ".\n");
    }

    static void searchByImdbID(HashedDictionary<String, Media> archive) {//gets imdbid and call getMediaDetails
        System.out.println("Enter an IMDbId ");
        Scanner scanner = new Scanner(System.in);
        String searchedIMDbID = scanner.nextLine().toUpperCase();
        System.out.println();
        getMediaDetails(searchedIMDbID, archive);
    }

    static void searchTest_1000(HashedDictionary<String, Media> archive) {
        String filePath = "C:/Users/Erol/OneDrive/Masaüstü/search.txt";
        long startTime = System.currentTimeMillis();  //start time
        int foundMedia = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String str;
            str = br.readLine();
            while ((str = br.readLine()) != null) {
                if (getMediaDetails(str, archive)) foundMedia++;
                System.out.println("-----------------------------------------\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();  // end time
        long duration = endTime - startTime;
        System.out.println("Searching completed in " + duration + " milliseconds\n" + foundMedia + " media Found.\n");
    }

    static void searchMediasByCountry(HashedDictionary<String, Media> archive) {
        //Travels whole archive with iterator
        System.out.println("Enter a country ");
        Scanner scanner = new Scanner(System.in);
        String country = scanner.nextLine();
        int count = 0;
        int countMedia = 0;
        int totalPlatform = 0;
        Iterator<Media> mediaIterator = archive.getValueIterator();
        System.out.println("\nHere is the list available medias in " + country + "!");
        while (mediaIterator.hasNext()) {
            count++;
            boolean found = false;
            Media media = mediaIterator.next();
            List<PlatformAndCountries> pncList = media.getPlatformAndCountries();
            Iterator<PlatformAndCountries> pncIterator = pncList.iterator();
            while (pncIterator.hasNext() && !found) {
                String[] countries = pncIterator.next().getCountires();
                totalPlatform++;
                for (int i = 0; i < countries.length; i++) {
                    if (countries[i].equals(country)) {
                        countMedia++;
                        found = true;
                        System.out.println(countMedia + " > " + media.getTitle());
                    }
                }
            }
        }
        System.out.println("\n" + countMedia + " medias are avaliable in "
                + country + " among " + archive.getSize() + " medias.");
    }

    static void allFivePlatforms(HashedDictionary<String, Media> archive) {
        //Travels whole archive with iterator and checks platformSize
        Iterator<Media> mediaIterator = archive.getValueIterator();
        int count = 0;
        while (mediaIterator.hasNext()) {
            Media media = mediaIterator.next();
            if (media.getPlatformSize() == 5) {
                count++;
                System.out.println(count + " > " + media.getTitle());
            }
        }
        System.out.println("\n" + count + " medias are streamed all 5 platforms!");
    }

    static void highestRatedMedias(HashedDictionary<String, Media> archive) {
        //Travels whole archive with iterator and compares rates.
        Iterator<Media> mediaIterator = archive.getValueIterator();
        double[] topTen = new double[10];
        String[] topTenTitles = new String[10];
        for (int i = 0; i < topTen.length; i++) {
            topTen[i] = 0;
            topTenTitles[i] = "";
        }
        while (mediaIterator.hasNext()) {
            Media media = mediaIterator.next();
            double mediaRate = media.getImdbAverageRating();
            String mediaTitle = media.getTitle();
            for (int i = 0; i < topTen.length; i++) {
                if (mediaRate > topTen[i]) {
                    for (int j = topTen.length - 1; j > i; j--) {
                        topTen[j] = topTen[j - 1];
                        topTenTitles[j] = topTenTitles[j - 1];
                    }
                    topTen[i] = mediaRate;
                    topTenTitles[i] = mediaTitle;
                    break;
                }
            }
        }
        System.out.println("Highest Rated Medias!");
        for (int i = 0; i < topTen.length - 1; i++) {
            System.out.println((i + 1) + " --> " + topTenTitles[i] + " --> " + topTen[i]);
        }
        System.out.println("10 -> " + topTenTitles[9] + " --> " + topTen[9] + "\n");
    }

    static boolean getMediaDetails(String imdbId, HashedDictionary<String, Media> archive) {
        //gets media details
        //returns true if finds media 
        if (archive.contains(imdbId)) {
            Media searchedMedia = archive.getValue(imdbId);
            System.out.println(searchedMedia.getTitle());
            System.out.println("Type: " + searchedMedia.getMediaType());
            System.out.println("Genre: " + searchedMedia.getGenres());
            System.out.println("Release Year: " + searchedMedia.getReleaseYear());
            System.out.println("IMDb ID: " + searchedMedia.getImdbId());
            System.out.println("IMDb Rating: " + searchedMedia.getImdbAverageRating());
            System.out.println("Number of Votes: " + searchedMedia.getImdbNumVotes() + "\n");
            searchedMedia.printPlatformAndCountries(searchedMedia.getPlatformAndCountries());
            return true;
        } else {
            System.out.println("Media not found!");
            return false;
        }
    }//end getMediaDetails

}
