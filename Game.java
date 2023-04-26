package project2;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Random;


public class Game {

    //String word;
    List<String> words_level1;
    String realWord;
    String shuffledWord;
    String currentGuess;
    File file; // first line is the realWord and below are the user attempts
    String fileContent;

    public Game() { // game constructor
        this.file = new File("input.txt");
        List<String> level1 = new ArrayList<>();
        level1.add("boat");
        level1.add("flower");
        level1.add("night");
        this.words_level1 = level1;
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
    public void chooseLevel() {
        System.out.println("Choose game level: (1, 2 or 3)");
        // deal with gameLevel later
        int gameLevel = Integer.parseInt(this.readUserInput())-1;

        Random rand = new Random();
        int int_random = rand.nextInt(this.words_level1.size());
        this.realWord = this.words_level1.get(int_random);
        this.writeToFile(this.realWord);
        System.out.println("Random Word: "+this.realWord);
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
        String inputString = this.realWord;
        // Convert the realWord to a char array
        char[] characters = inputString.toCharArray();

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

