import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Main
{
    public static void main(String[] args) throws IOException, WavFileException {
            //Open Dialog Box
            final JFileChooser fc = new JFileChooser();
            fc.showOpenDialog(null);

            // Open the wav file specified as the first argument
            WavFile wavFile = WavFile.openWavFile(fc.getSelectedFile());

            // Display information about the wav file
            wavFile.display();

            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();

            // Create a buffer of 100 frames
            double[] buffer = new double[100 * numChannels];

            int framesRead;

            ArrayList<String> allValues = new ArrayList<>();
            ArrayList<String> allValues2 = new ArrayList<>();

            do
            {
                // Read frames into buffer
                framesRead = wavFile.readFrames(buffer, 100);

                // Loop through frames and look for minimum and maximum value
                for (int s=0 ; s<framesRead * numChannels ; s++)
                {
                    allValues.add("" + buffer[s]);
                    allValues2.add("" + buffer[s]);
                }
            } while (framesRead != 0);

            LZWEncoding(allValues, wavFile.getNumFrames());
            Huffman(allValues2, wavFile.getNumFrames());

            // Close the wavFile
            wavFile.close();
    }

    //LZW Based on Submitted Programming Assignment 2 Code and Class Note Pseudo Code
    public static void LZWEncoding (ArrayList<String> allValues, long size) {
        //Implement Dictionary Here
        LinkedHashMap<String, Integer> dictionary = new LinkedHashMap<>();

        StringBuilder output = new StringBuilder();

        //List the first Integer Available after Dictionary Completed
        int availableCode = 0;

        //LZW Implementation Here
        String S = allValues.get(0);
        allValues.remove(0);
        while(allValues.size() != 0) {
            String C = allValues.get(0);
            allValues.remove(0);

            //S + C
            String currentPair = S + " " + C;
            if(dictionary.containsKey(currentPair)) {
                S = currentPair;
            }else {
                output.append("" + dictionary.get(S));
                dictionary.put(currentPair, availableCode);
                availableCode++;
                S = C;
            }
        }
        System.out.println("LZW Dictionary Size: " + dictionary.size());
        System.out.println("Output Length: " + output.length());
        System.out.println("My Algorithm LZW Compression Ratio: " + ((double)dictionary.size()/size));
    }

    public static void Huffman(ArrayList<String> allValues, long size) {
        //Change Input String Here
        double totalElements = allValues.size();

        //Create HashMap of Single Characters Here
        HashMap<String, Integer> singleEntropy = new HashMap<>();
        while(allValues.size() != 0) {
            String currentChar = allValues.get(0);
            allValues.remove(0);
            if (singleEntropy.containsKey(currentChar)) {
                singleEntropy.put(currentChar, singleEntropy.get(currentChar) + 1);
            } else {
                singleEntropy.put(currentChar, 1);
            }
        }

        //Calculate First order Entropy
        double firstOrderEntropy = 0;
        for (String key : singleEntropy.keySet()) {
            double currentValue = singleEntropy.get(key);
            double probability = currentValue / totalElements;
            firstOrderEntropy = firstOrderEntropy + (-((probability) * (Math.log(probability) / Math.log(2))));
        }

        System.out.println("First-order entropy: " + firstOrderEntropy);

        //Huffman Coding
        ArrayList<Integer> singleOrderValues = new ArrayList<>();
        for (int value : singleEntropy.values()) {
            singleOrderValues.add(value);
        }

        //Create Priority Queue with Values Sorted Based on PPT
        Collections.sort(singleOrderValues);
        ArrayList<CustomNode> nodeList = new ArrayList<>();
        for (int i = 0; i < singleOrderValues.size(); i++) {
            CustomNode tempCustomNode = new CustomNode(singleOrderValues.get(i), false);
            nodeList.add(tempCustomNode);
        }

        CustomNode root = null;

        while (nodeList.size() > 1) {
            //Get First CustomNode
            CustomNode lowestMinCustomNode = nodeList.remove(0);
            //Get Second CustomNode
            CustomNode secondMinCustomNode = nodeList.remove(0);
            //New Parent CustomNode of Huffman Subtree
            int parentNodeValue = lowestMinCustomNode.value + secondMinCustomNode.value;

            CustomNode parentCustomNode = new CustomNode(parentNodeValue, true);

            parentCustomNode.leftChild = lowestMinCustomNode;
            parentCustomNode.rightChild = secondMinCustomNode;

            root = parentCustomNode;

            Boolean isInserted = false;

            for (int i = 0; i < nodeList.size(); i++) {
                if (parentCustomNode.value < nodeList.get(i).value) {
                    nodeList.add(i, parentCustomNode);
                    isInserted = true;
                    break;
                }
            }

            if (!isInserted) {
                nodeList.add(parentCustomNode);
            }
        }

        //Insert code length for Single into Array
        ArrayList<Integer> singleCodeLengths = new ArrayList<>();
        traverseCustomNode(root, 0, singleCodeLengths);
        //Sort based on Descending Order
        Collections.sort(singleCodeLengths, Collections.reverseOrder());

        double singleHuffmanAverage = 0;

        for(int i = 0; i < singleOrderValues.size(); i++) {
            double probability = singleOrderValues.get(i) / totalElements;

            singleHuffmanAverage = singleHuffmanAverage + (probability * singleCodeLengths.get(i));
        }

        System.out.println("Average codeword lengths for Huffman coding: " + singleHuffmanAverage);

        System.out.println("My Algorithm Huffman Compression Ratio: " + ((double)singleEntropy.size()/size));
    }

    public static ArrayList<Integer> traverseCustomNode(CustomNode root, int pathLength, ArrayList<Integer> mainArrayList)
    {
        if (root.leftChild == null && root.rightChild == null
                && root.parent == false) {
            mainArrayList.add(pathLength);
        }
        if(root.rightChild != null) {
            traverseCustomNode(root.rightChild, pathLength + 1, mainArrayList);
        }
        if(root.leftChild != null) {
            traverseCustomNode(root.leftChild, pathLength + 1, mainArrayList);
        }
        return mainArrayList;
    }
}
