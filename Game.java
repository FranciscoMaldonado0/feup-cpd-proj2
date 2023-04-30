package project2;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Random;


public class Game {

    //String word;
    List<List<String>> levels;
    String realWord;
    String shuffledWord;
    String currentGuess;
    File file; // first line is the realWord and below are the user attempts
    String fileContent;

    public Game() { // game constructor
        this.file = new File("input.txt");
        this.levels = new ArrayList<>();

        List<String> level1 = new ArrayList<>();
        level1.add("boat");
        level1.add("flower");
        level1.add("night");
        this.levels.add(level1);

        List<String> level2 = new ArrayList<>();
        level2.add("library");
        level2.add("elephant");
        level2.add("window");
        level2.add("mistake");
        this.levels.add(level2);

        List<String> level3 = new ArrayList<>();
        level3.add("mushrooom");
        level3.add("disinterested");
        level3.add("kitchen");

        this.levels.add(level3);
    }
    public static void main(String[] args) {

        Game game = new Game();

        //game.setWordToGuess();
        // put the word in the first line of the game file:
        //game.writeToFile(game.realWord);

        // cleans the file, to start another game
        game.cleanFile();
        // chooses the word of the game, and write it in the first line of teh file
        game.chooseLevel();
        // generate the shuffleWord
        game.generateWord();

        game.gameLoop();

    }
    public void cleanFile(){
        try {
            File file = this.file;
            FileWriter fileWriter = new FileWriter(file);
            String allToWrite = "";
            fileWriter.write(allToWrite);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred while writing to file.");
            e.printStackTrace();
        }
    }

    public void checkInput(){
        Scanner input = new Scanner(System.in);
        String userInput;
        boolean isValid = false;

        while(!isValid){
            System.out.print("Enter your input: ");
            userInput = input.nextLine();

            try{
                int user_input = Integer.parseInt(userInput);
                if (user_input > 0 && user_input < 3) {
                    isValid = true;
                }
                if (isValid) {
                    Random rand = new Random();
                    int gameLevel = user_input-1;
                    int int_random = rand.nextInt(this.levels.get(gameLevel).size());
                    this.realWord = this.levels.get(gameLevel).get(int_random);
                    this.writeToFile(this.realWord);
                    System.out.println("Random Word: "+this.realWord);
                }
                else{
                    System.out.println("Invalid input. Please try again.");
                }
            }
            catch (Exception ignored){
                System.out.println("Enter a valid option: ");
            }
        }
    }

    public void chooseLevel() {
        System.out.println("Choose game level: (1, 2 or 3)\n");
        this.checkInput();
    }
    public void gameLoop() {
        int i = 0;
        while (!Objects.equals(this.realWord, this.currentGuess)){
            if (i != 0){
                System.out.println("\nIncorrect answer.");
            }
            System.out.println("Enter your guess: ");
            this.writeToFile(null);
            i++;
        }
        System.out.println("\nYOU WIN!");

    }
    public void setWordToGuess(){
        System.out.println("\nChoose the word to guess: ");
        this.realWord = readUserInput();
        System.out.println("\nWord to guess: "+this.realWord);

    }
    public void generateWord() {
        // Convert the realWord to a char array
        char[] characters = this.realWord.toCharArray();

        Random r = new Random();
        // Iterate through the char array
        for (int i = 0; i < characters.length; i++) {
            // Generate a random number between 0 to length of char array
            int randomIndex = r.nextInt(characters.length);
            // Swap the characters
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }

        // Initialize shuffledWord
        this.shuffledWord = new String(characters);
        // Print the shuffled string
        System.out.println('\n'+"Word to show: "+this.shuffledWord);
    }
    public String readUserInput(){
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
    public void writeToFile(String word){
        String lineToWrite;
        if (word == null){
            lineToWrite = readUserInput();
            this.currentGuess = lineToWrite;
            this.getFileContent();
            try {
                File file = this.file;
                FileWriter fileWriter = new FileWriter(file);
                String allToWrite = this.fileContent+lineToWrite;
                fileWriter.write(allToWrite);
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred while writing to file.");
                e.printStackTrace();
            }
        }
        else{
            try {
                File file = this.file;
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(word);
                this.fileContent += word;
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred while writing Word to file.");
                e.printStackTrace();
            }
        }
    }
    public void getFileContent() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String fileContent = "";
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent += line + '\n';
            }
            this.fileContent = fileContent;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

