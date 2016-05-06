/**
 * This is the Easy Loop application, software capable of looping audio files at
 * different pitches. Useful for live performance and fun experimentation
 * Made for CMPT 166. * Pitch shifting and tempo changing made possible by
 * "soundStretch"
 * 
 * @see http://www.surina.net/soundtouch/soundstretch.html
 * 
 * @author David Hampson (DavidHampson.97@gmail.com)
 */
package easyLoop;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class EasyLoop extends JFrame implements ActionListener {
	
	/**
	 * Serial ID to ensure version continuity
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Run the GUI
	 * @param args command line Args
	 * @throws IOException input/output errors
	 */
	public static void main (String[] args) throws IOException {
		EasyLoop gui = new EasyLoop();
		
		// We can't deal without resources
		if (!(new File("resources").exists())) {
			JOptionPane.showMessageDialog(null, "Missing files, exiting.");
			System.exit(-1);
		}
		
		// Create the files we need
		if (!(new File("tracks").exists())
		|| !(new File("saves").exists())) {
			Boolean trackFile = (new File(TRACK_DIRECTORY)).mkdirs();
			Boolean savesFile = (new File(SAVES_DIRECTORY)).mkdirs();
			if (!trackFile || !savesFile) {
				JOptionPane.showMessageDialog(null, "File creation failed, "
						+ "please create a \"tracks\" folder and a \"saves\" "
						+ "folder in this directory. Exiting.");
				System.exit(-1);
			}
		}
		
		gui.setVisible(true);
		gui.pack();
	}
	
	/**
	 * Hotkeys
	 */
	public final static String PLAY_HOTKEY = "P";
	public final static String PREPARE_HOTKEY = "P";
	public final static String LOOP_HOTKEY = "O";
	public final static String RECORDING_HOTKEY = "ctrl ";
	public final static String IMPORT_HOTKEY = "shift ";
	public final static String REPS_HOTKEY = "alt ctrl ";
	public final static String PATTERN_HOTKEY = "alt ";
	public final static String[] PLAYING_HOTKEYS = {"1", "2", "3", "4", "5",
			"6", "7", "8", "9", "0"};
	
	/**
	 * So the load function knows how many values to cycle through
	 */
	public final static int SAVE_ARRAY_LENGTH = 3;
	
	/**
	 * Extension
	 */
	public final static String EXTENTION = ".wav";
	
	/**
	 * Min and max transpose
	 */
	
	public final static int MIN_TRANSPOSE = -60;
	public final static int MAX_TRANSPOSE = 60;
	public final static int MIN_STRETCH = -95;
	public final static int MAX_STRETCH = 5000;

	/**
	 * Directory
	 */
	public final static String DIRECTORY = System.getProperty("user.dir")+"/";
	public final static String TRACK_DIRECTORY = DIRECTORY + "tracks/";
	public final static String SAVES_DIRECTORY = DIRECTORY + "saves/";
	
	/**
	 * Main Layout
	 */
	public final static int WRAPPER_PADDING = 10;
	
	/**
	 * Height of buttons JPanel
	 */
	public final static int BUTTONS_HEIGHT = 40;
	
	/**
	 * Height of menu bar
	 */
	public final static int MENU_HEIGHT = 20;
	
	/**
	 * Tracks per row of JPanel
	 */
	public final static int TRACKS_PER_ROW = 5;
	
	/**
	 * Max rows of tracks
	 */
	public final static int MAX_ROWS = 2;
	
	/**
	 * Current amount of rows
	 */
	private int currentRows = 0;
	
	/**
	 * Width of main panel
	 */
	private int width = 190;
	
	/**
	 * Height of main panel
	 */
	private int height = 400;
	
	/**
	 * Width of track JLabel
	 */
	public final static int TRACK_WIDTH = 190;
	
	/**
	 * Height of track JLabel
	 */
	public final static int TRACK_HEIGHT = 400;
	
	/**
	 * Track JLabel dimension
	 */
	public final static Dimension TRACK_SIZE = 
			new Dimension(TRACK_WIDTH, TRACK_HEIGHT);
	
	/**
	 * Layout for track JLabel 
	 */
	public final static GridLayout TRACK_LAYOUT = new GridLayout(9,1);

	/**
	 * Layout for (import/record) box
	 */
	public final static GridLayout INPUT_LAYOUT = new GridLayout(1,2);

	/**
	 * Spacing for main panel
	 */
	public final static int TRACK_MARGIN = 5;
	
	/**
	 * Layout for main panel
	 */
	public final static FlowLayout MAIN_LAYOUT = 
			new FlowLayout(FlowLayout.CENTER, TRACK_MARGIN, TRACK_MARGIN);

	/**
	 * Padding for track border
	 */
	public final static int TRACK_PADDING = 5;
	
	/**
	 * Border to add padding to track
	 */
	public final static EmptyBorder TRACK_BORDER = new EmptyBorder(
			TRACK_PADDING, TRACK_PADDING, 
			TRACK_PADDING, TRACK_PADDING);

	/**
	 * Number of tracks
	 */
	 int trackNumber = 0;
	 
	 /**
	  * int to track which track did what
	  */
	 int eventIndex = 0;

	/**
	 * Index of main track
	 */
	 int mainTrackIndex = -1;
	
	/**
	 * borders for tracks
	 */
	 ArrayList<TitledBorder> trackBorderList = 
			new ArrayList<TitledBorder>();
	
	/**
	 * Box to house (import/record) buttons
	 */
	ArrayList<JPanel> inputBoxList = 
			new ArrayList<JPanel>();

	
	/**
	 * JPanels for tracks
	 */
	private ArrayList<JPanel> trackPanelList = 
			new ArrayList<JPanel>();
	
	/**
	 * Panel for housing buttons
	 */
	private JPanel buttonPanel;
	
	/**
	 * Panel for housing tracks
	 */
	private JPanel mainPanel;

	/**
	 * Integers representing the current repetition
	 */
	 ArrayList<Integer> currentList = 
			new ArrayList<Integer>();
	
	/**
	 * Integers representing the max amount of repetitions
	 */
	 ArrayList<Integer> maxList = 
			new ArrayList<Integer>();
	
	/**
	 * Length of loop for PlayManager to use
	 */
	Double loopLength = .75;
	
	/**
	 * Format for loop length to be put in
	 */
	NumberFormat loopFormat = new DecimalFormat("##.##");
	
	/**
	 * Button for starting play
	 */
	JButton playButton;
	
	/**
	 * PlayManager
	 */
	PlayManager playManager = new PlayManager(this);
	
	
	/**
	 * Button for preparing tracks
	 */
	private JButton prepareButton;
	
	/**
	 * Button for setting loop length
	 */
	private JButton setLoopLength;

	/**
	 * Buttons for setting track pattern
	 */
	private ArrayList<JButton> setPatternButtonList =
			new ArrayList<JButton>();
	
	/**
	 * Buttons for importing
	 */
	private ArrayList<JButton> importButtonList =
			new ArrayList<JButton>();
	
	/**
	 * Buttons for recording to a track
	 */
	private ArrayList<JButton> recordButtonList =
			new ArrayList<JButton>();
	
	/**
	 * Recording dialog choice
	 */
	private int choice = 0;
	
	/**
	 * Monitor recording state
	 */
	private boolean recording = false;
	
	/**
	 * Buttons for setting track reps
	 */
	private ArrayList<JButton> occButtonList =
			new ArrayList<JButton>();
	
	/**
	 * Labels to display the track pattern
	 */
	private ArrayList<JLabel> patternLabelList = 
			new ArrayList<JLabel>();

	/**
	 * Labels to show what transposition
	 * is to be played next
	 */
	ArrayList<JLabel> nextToPlayList = 
			new ArrayList<JLabel>();

	/**
	 * Labels to show how many repetitions
	 * a track will do per loop
	 */
	private ArrayList<JLabel> occList = 
			new ArrayList<JLabel>();

	/**
	 * Labels to countdown to the next repetition 
	 */
	ArrayList<JLabel> countdownList = 
			new ArrayList<JLabel>();

	/**
	 * Checkbox to decide what tracks play
	 */
	protected ArrayList<JCheckBox> playingList =
			new ArrayList<JCheckBox>();
	
	/**
	 * Radio buttons for choosing master lists
	 */
	private ArrayList<JRadioButton> isMasterList = 
			new ArrayList<JRadioButton>();
	
	/**
	 * JMenu
	 */
	
	private JMenuBar menuBar;
	
	private JMenu edit;
	private JMenuItem addTrack;
	private JMenuItem removeTrack;
	private JMenuItem resetTracks;
	private JMenuItem timeStretch;
	
	private JMenu file;
	private JMenuItem quit;
	private JMenuItem save;
	private JMenuItem open;

	/**
	 * Ctrl + N Keystroke
	 */
	private KeyStroke ctrlN = KeyStroke.getKeyStroke(KeyEvent.VK_N, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	/**
	 * Ctrl + R Keystroke
	 */
	private KeyStroke ctrlR = KeyStroke.getKeyStroke(KeyEvent.VK_R, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	/**
	 * Ctrl + T Keystroke
	 */
	private KeyStroke ctrlT = KeyStroke.getKeyStroke(KeyEvent.VK_T, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	
	/**
	 * Ctrl + S Keystroke
	 */
	private KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	/**
	 * Ctrl + O Keystroke
	 */
	private KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

	/**
	 * Ctrl + B Keystroke
	 */
	private KeyStroke ctrlB = KeyStroke.getKeyStroke(KeyEvent.VK_B, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	/**
	 * Ctrl + Q Keystroke
	 */
	private KeyStroke ctrlQ = KeyStroke.getKeyStroke(KeyEvent.VK_Q, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	/**
	 * Audio Tracks
	 */
	protected ArrayList<AudioTrack> trackList = new ArrayList<AudioTrack>();
	
	/**
	 * Sequences for transposition
	 */
	ArrayList<ArrayList<Object>> patternList = 
			new ArrayList<ArrayList<Object>>();
	
	/**
	 * For recording
	 */
	String recordingFile;
	
	/**
	 * Adds a new track to the JFrame
	 */
	private void addTrack () {
		// Add a JLabel to all componenets that need one
		patternLabelList.add(new JLabel("0"));
		nextToPlayList.add(new JLabel("Next Transposition: 0"));
		occList.add(new JLabel("Rate of Occurrence: 1"));
		countdownList.add(new JLabel("Reps till next play: 0"));
		
		// Add a JButton to all components that need one
		setPatternButtonList.add(new JButton("Set Pattern"));
		importButtonList.add(new JButton("Import"));
		recordButtonList.add(new JButton("Record"));
		occButtonList.add(new JButton("Set Rate of Ocurrence"));
		
		maxList.add(1);
		currentList.add(1);
		
		ArrayList<Object> newPattern = new ArrayList<Object>();
		newPattern.add(0); // We need a base value
		
		patternList.add(newPattern);
		
		// Add a new checkbox and radio button
		playingList.add(new JCheckBox("Playing", false));
		isMasterList.add(new JRadioButton("Main Track"));
	
		// Add a new track
		trackList.add(new AudioTrack());
		
		// JPanels
		trackPanelList.add(new JPanel());
		inputBoxList.add(new JPanel());
		
		// Increment the row if we are at the end of a line
		if (trackNumber%TRACKS_PER_ROW == 0) currentRows ++;
		 
		if (trackNumber == TRACKS_PER_ROW * MAX_ROWS) {
			addTrack.setEnabled(false);
		}
		
		// Create Border
		trackBorderList.add(new TitledBorder(""));
		
		// Create Panel
		trackPanelList.get(trackNumber).setBackground(Color.WHITE);
		trackPanelList.get(trackNumber).setLayout(TRACK_LAYOUT);
		
		trackPanelList.get(trackNumber).
			setBorder(trackBorderList.get(trackNumber));
		trackPanelList.get(trackNumber).setPreferredSize(TRACK_SIZE);
		
		// Radio Button
		isMasterList.get(trackNumber).setActionCommand("Main"+trackNumber);
		isMasterList.get(trackNumber).addActionListener(this);
		isMasterList.get(trackNumber).setEnabled(false);
		
		trackPanelList.get(trackNumber).add(isMasterList.get(trackNumber));
		
		// Labels
		trackPanelList.get(trackNumber).add(patternLabelList.get(trackNumber));
		trackPanelList.get(trackNumber).add(nextToPlayList.get(trackNumber));
		trackPanelList.get(trackNumber).add(occList.get(trackNumber));
		
		// Record and import
		inputBoxList.get(trackNumber).setLayout(INPUT_LAYOUT);
		
		importButtonList.get(trackNumber).setActionCommand("Import" 
				+ trackNumber);
		importButtonList.get(trackNumber).setBackground(Color.WHITE);
		importButtonList.get(trackNumber).addActionListener(this);
		inputBoxList.get(trackNumber).add(importButtonList.get(trackNumber));
	
		recordButtonList.get(trackNumber).setActionCommand("Record" 
				+ trackNumber);
		recordButtonList.get(trackNumber).setBackground(Color.WHITE);
		recordButtonList.get(trackNumber).addActionListener(this);
		inputBoxList.get(trackNumber).add(recordButtonList.get(trackNumber));
		
		trackPanelList.get(trackNumber).add(inputBoxList.get(trackNumber));
		
		// Set pattern and reps
		setPatternButtonList.get(trackNumber).setActionCommand("Set Pattern" 
				+ trackNumber);
		setPatternButtonList.get(trackNumber).setBackground(Color.WHITE);
		setPatternButtonList.get(trackNumber).addActionListener(this);
		setPatternButtonList.get(trackNumber).setEnabled(false);
		trackPanelList.get(trackNumber)
				.add(setPatternButtonList.get(trackNumber));
		occButtonList.get(trackNumber).setActionCommand("Set Rate of Occurrence"
				+ trackNumber);
		occButtonList.get(trackNumber).setBackground(Color.WHITE);
		occButtonList.get(trackNumber).addActionListener(this);
		occButtonList.get(trackNumber).setEnabled(false);
		trackPanelList.get(trackNumber).add(occButtonList.get(trackNumber));
		
		// Playing box
		playingList.get(trackNumber).setEnabled(false);
		
		trackPanelList.get(trackNumber).add(playingList.get(trackNumber));
		
		// Countdown
		trackPanelList.get(trackNumber).add(countdownList.get(trackNumber));
		
		mainPanel.add(trackPanelList.get(trackNumber));

		// Set hotkeys
		int hotkeyNumber = new Integer(trackNumber);

		trackNumber ++;
		
		if (trackNumber > 1) {
			removeTrack.setEnabled(true);
		}
		
		// Set title
		trackBorderList.get(trackNumber-1).setTitle("Track "+trackNumber);
		
		// The tenth track should be accessable with 0
		if (trackNumber == 9) hotkeyNumber = 0;
		
		// Imports
		PressButton.setHotkey(importButtonList.get(hotkeyNumber),
				IMPORT_HOTKEY+trackNumber,
				importButtonList.get(hotkeyNumber).getActionCommand());
		
		// Reps
		PressButton.setHotkey(occButtonList.get(hotkeyNumber),
				REPS_HOTKEY+trackNumber,
				occButtonList.get(hotkeyNumber).getActionCommand());
		
		// Patterns
		PressButton.setHotkey(setPatternButtonList.get(hotkeyNumber),
						PATTERN_HOTKEY+trackNumber,
						setPatternButtonList.get(hotkeyNumber).
						getActionCommand());
		
		// Recording
		PressButton.setHotkey(recordButtonList.get(hotkeyNumber),
						RECORDING_HOTKEY+trackNumber,
						recordButtonList.get(hotkeyNumber).
						getActionCommand());
		
		// Mute/unmute
		PressButton.setHotkey(playingList.get(hotkeyNumber),
				PLAYING_HOTKEYS[hotkeyNumber],
				playingList.get(hotkeyNumber).
				getActionCommand());
		
		// Increase track number

		updateWindowSize();

	}
	
	private void removeTrack (int whichOne) {
		
		patternLabelList.get(whichOne).setVisible(false);
		nextToPlayList.get(whichOne).setVisible(false);
		occList.get(whichOne).setVisible(false);
		countdownList.get(whichOne).setVisible(false);
		setPatternButtonList.get(whichOne).setVisible(false);
		importButtonList.get(whichOne).setVisible(false);
		recordButtonList.get(whichOne).setVisible(false);
		occButtonList.get(whichOne).setVisible(false);
		playingList.get(whichOne).setVisible(false);
		isMasterList.get(whichOne).setVisible(false);
		trackPanelList.get(whichOne).setVisible(false);
		inputBoxList.get(whichOne).setVisible(false);
		
		patternLabelList.remove(whichOne);
		nextToPlayList.remove(whichOne);
		occList.remove(whichOne);
		countdownList.remove(whichOne);
		setPatternButtonList.remove(whichOne);
		importButtonList.remove(whichOne);
		recordButtonList.remove(whichOne);
		occButtonList.remove(whichOne);
		maxList.remove(whichOne);
		currentList.remove(whichOne);
		patternList.remove(whichOne);
		playingList.remove(whichOne);
		isMasterList.remove(whichOne);
		trackList.remove(whichOne);
		trackPanelList.remove(whichOne);
		inputBoxList.remove(whichOne);
		trackBorderList.remove(whichOne);
		
		trackNumber --;
		if (trackNumber == 1) {
			removeTrack.setEnabled(false);
		}
		
		if (trackNumber % TRACKS_PER_ROW == 0) {
			currentRows --;
		}
		updateWindowSize();
		
		revalidate();
		repaint();
		
	}
	
	private void updateWindowSize () {
		width = TRACKS_PER_ROW *
				(
					TRACK_WIDTH + 
					TRACK_MARGIN + 
					TRACK_MARGIN
				) +
				WRAPPER_PADDING +
				WRAPPER_PADDING;
			
		// Calculate height
		height = currentRows *
				 (
					TRACK_HEIGHT +
					TRACK_MARGIN +
					TRACK_MARGIN
				 ) + 
				 WRAPPER_PADDING;
		
		// Update
		mainPanel.setPreferredSize(new Dimension(width, height));
		this.revalidate();
		this.repaint();
		this.pack();
	}
	
	/**
	 * Main panel
	 */
	public EasyLoop () {
		super("Easy Loop");

		setSize(width, height);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		// Main Panel for housing tracks
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.PINK);
		mainPanel.setLayout(MAIN_LAYOUT);
		
		// Add a track
		addTrack();
		
		// Create the panel to host the buttons
		buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(width, BUTTONS_HEIGHT));
		buttonPanel.setBackground(Color.LIGHT_GRAY);
		buttonPanel.setLayout(new FlowLayout());

		// Create the buttons to color the panels
		playButton = new JButton("Play");
		playButton.setBackground(Color.WHITE);
		playButton.addActionListener(this);
		
		playButton.setEnabled(false); // we need to import first
		buttonPanel.add(playButton);
		
		prepareButton = new JButton("Prepare");
		prepareButton.setBackground(Color.WHITE);
		prepareButton.addActionListener(this);
		prepareButton.setEnabled(false);
		buttonPanel.add(prepareButton);
		
		setLoopLength = new JButton("Set Loop Length (Current: " 
				+ loopFormat.format(loopLength) + ")");
		setLoopLength.setBackground(Color.WHITE);
		setLoopLength.setActionCommand("Set Loop Length");
		setLoopLength.addActionListener(this);
		buttonPanel.add(setLoopLength);
		
		
		// Create menu bar
		menuBar = new JMenuBar();
		menuBar.setPreferredSize(new Dimension(width, MENU_HEIGHT));
		
		file = new JMenu("File");
		
		quit = new JMenuItem("Quit");
		quit.setAccelerator(ctrlQ);
		quit.addActionListener(this);
		
		save = new JMenuItem("Save");
		save.setAccelerator(ctrlS);
		save.addActionListener(this);
		
		open = new JMenuItem("Open");
		open.setAccelerator(ctrlO);
		open.addActionListener(this);
		
		edit = new JMenu("Edit");
		
		addTrack = new JMenuItem("Add Track");
		addTrack.setAccelerator(ctrlN);
		addTrack.addActionListener(this);
		
		removeTrack = new JMenuItem("Remove Track");
		removeTrack.setAccelerator(ctrlR);
		removeTrack.addActionListener(this);
		
		resetTracks = new JMenuItem("Reset");
		resetTracks.setAccelerator(ctrlT);
		resetTracks.addActionListener(this);
		
		timeStretch = new JMenuItem("Change BPM of file");
		timeStretch.setAccelerator(ctrlB);
		timeStretch.addActionListener(this);
		
		menuBar.add(file);

		file.add(quit);
		file.add(save);
		file.add(open);
		
		menuBar.add(edit);
		
		edit.add(addTrack);
		edit.add(removeTrack);
		edit.add(resetTracks);
		edit.add(timeStretch);
		
		// Set Hotkeys
		PressButton.setHotkey(playButton, PLAY_HOTKEY, "Play");
		PressButton.setHotkey(prepareButton, PREPARE_HOTKEY, "Prepare");
		PressButton.setHotkey(setLoopLength, LOOP_HOTKEY, "Set Loop Length");
		
		// Add our created elements to the layout
		add(menuBar, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Calculate the length of the file found at /fileName in seconds
	 * @see http://stackoverflow.com/questions/3009908/how-do-i-get-a-sound-files-total-time-in-java
	 * 
	 * @param fileName file to calculate
	 * @return double of length in seconds
	 */
	public double calculateLength (String fileName) {
		try {
			AudioInputStream audioInputStream = 
					AudioSystem.getAudioInputStream(new File(fileName));
			AudioFormat format = audioInputStream.getFormat();
			
			double frames = audioInputStream.getFrameLength();

			audioInputStream.close();
			
			return (long)(frames) / format.getFrameRate();

		}
		catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Unexpected Error while "
					+ "calculating length of file.");
			ex.printStackTrace();	
		} 
		catch (UnsupportedAudioFileException ex) {
			JOptionPane.showMessageDialog(null, "File not found.");
		}
		return 0.0;
	}
	
	/**
	 * Converts the state of the program to a text file at
	 * a speficied location
	 * 
	 * @param saveLocation location to save to
	 */
	private void save(String saveLocation) {
		
		saveLocation += ".txt";
		
		// Save data as text
		String saveFile = "";
		saveFile += trackNumber + "@" + loopLength + "@" + mainTrackIndex + "@";
	
		for (int i = 0; i < trackNumber; i++) {
			
			saveFile += trackList.get(i).track + "@";
			for (Object num : patternList.get(i)) {
				saveFile += num + ",";
			}
			saveFile += "@" + playingList.get(i).isSelected() + "@";
			saveFile += maxList.get(i) + "@";
			saveFile += currentList.get(i) + "@";
		}
		
		try {
			
			// Create the file
			new File(saveLocation);
			
			// Write the line
			PrintWriter writer = new PrintWriter(saveLocation, "UTF-8");
			writer.println(saveFile);
			writer.close();
		}
		
		catch (IOException e) {
			JOptionPane.showMessageDialog(null, 
					"Error with saving file.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the program state based on a file
	 * @param saveFile file to load
	 * @throws IOException If there is a problem with reading the file
	 * @throws NullPointerException If the file is bad
	 */
	private void load(String loadFile) throws IOException, 
		NullPointerException, NumberFormatException {
		
		// Load the file to a track
		FileReader reader = new FileReader(loadFile);
		BufferedReader bufferedReader = new BufferedReader(reader);
		String saveFile = bufferedReader.readLine();
		reader.close();
		
		// Clear tracks
		while (trackNumber > 0) {
			removeTrack(0);
		}
		
		String[] saveArray = saveFile.split("@");
		
		loopLength = Double.valueOf(saveArray[1]);
		
		mainTrackIndex = Integer.valueOf(saveArray[2]);

		int saveIndex = SAVE_ARRAY_LENGTH;
		for (int i = 0; i < Integer.parseInt(saveArray[0]); i++) {
			addTrack(); 
			
			if (i == mainTrackIndex) {
				isMasterList.get(i).setEnabled(true);
				isMasterList.get(i).setSelected(true);
			}
			
			if (!saveArray[saveIndex].equals("null")) {
				// Set track
				trackList.get(i).setTrack(
					new File(saveArray[saveIndex]));
				
				// Modify button states
				playingList.get(i).setEnabled(true);
				playingList.get(i).setSelected(true);
				setPatternButtonList.get(i).setEnabled(true);
				isMasterList.get(i).setEnabled(true);
				
				// Set all reps buttons to avaliable if this isn't
				// the main track
				occButtonList.get(i)
					.setEnabled(i != mainTrackIndex);
				
	        	trackBorderList.get(i).setTitle(trackList.get(i).trackName + 
	        			EXTENTION);
			}
			
			saveIndex ++;
			
			// Generate pattern from text
			String[] savePattern =
					saveArray[saveIndex].split(",");
			
			String patternString = "";
			
			patternList.get(i).clear();
			
			for (String s : savePattern) {
				if (!s.equals("!") && !s.equals(">") && !s.equals("|")) {
					patternList.get(i).
						add(Integer.parseInt(s));
				}
				else {
					patternList.get(i).add(s);
				}
				patternString += s + " ";		
			}
			
			// Compact concurrent sequences
			patternString = patternString.replace(" | ", "|");	
		
			saveIndex ++;

			// Set playing state
			if (saveArray[saveIndex].equals("true")) {
				playingList.get(i).setSelected(true);
			}
			
			saveIndex ++;
			
			maxList.set( i,
					Integer.valueOf(saveArray[saveIndex]));
			
			saveIndex ++;
			
			currentList.set( i,
					Integer.valueOf(saveArray[saveIndex]));
			
			saveIndex ++;

			// Set displays
			setLoopLength.setText("Set Loop Length (Current: " 
					+ loopFormat.format(loopLength) + ")");
			
			patternLabelList.get(i).setText("Pattern: " 
					+ patternString);
			
        	nextToPlayList.get(i).setText("Next transposition: " 
					+ patternList.get(i).get(0));				
        	
        	occList.get(i).setText("Rate of Occurrence: " 
					+ maxList.get(i));
		}
		prepareButton.setEnabled(true);
		playButton.setEnabled(false);
	}
		
	/**
	 * ActionListner
	 * 
	 * @ActionEvent the event sent
	 */
	public void actionPerformed (ActionEvent e) {
		//Check which button was pressed and change color based on that 
		String event = e.getActionCommand();
				
		// Get index of event
		try {
			eventIndex = Integer.parseInt(event.substring(event.length()-1));
		}
		
		// We don't need to do anything, this just
		// tells us that this particular event isnt
		// one that ends in an int
		catch (NumberFormatException ex) {}; 
		
		// Add a track
		if (event.equals("Add Track")) {
			addTrack();
		}
		
		else if (event.equals("Remove Track")) {
			String removeInt = JOptionPane.showInputDialog("Which track?");
			
			// Make sure it is an int and a track
			boolean pass = true;
			
			try {
				if (removeInt != null) {
					// -1 for index syntax
					if (Integer.valueOf(removeInt)-1 < 0 
							|| Integer.valueOf(removeInt) > trackList.size()) {
						JOptionPane.showMessageDialog(null, 
								"Track does not exist.");
						pass = false;
					}
				}
				else {
					pass = false;
				}
			}
			
			catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(null, 
						"Please input an integer.");
				pass = false;
			}
			
			// Make sure it is only ints
			if (pass) {	
				removeTrack(Integer.valueOf(removeInt)-1);
			}
		}
		
		// Reset to base state
		else if (event.equals("Reset")) {
			
			while (trackNumber > 0) {
				removeTrack(0);
			}

			addTrack();
			
			this.setTitle("Easy Loop");
			
			playButton.setEnabled(false);
			prepareButton.setEnabled(false);
		}
		
		else if (event.equals("Open")) {
			// Create a dialog to load a file
			JFileChooser dialog = 
					new JFileChooser(SAVES_DIRECTORY);
			
			// Make sure the file is a wav or a midi
			FileNameExtensionFilter filter = 
					new FileNameExtensionFilter("*.txt", "txt");
			dialog.setFileFilter(filter);

			// Open the dialog
			int dialogOpen = dialog.showOpenDialog(this);
		    
			File loadedFile = null;
			
			// When we choose the file process it
			if(dialogOpen == JFileChooser.APPROVE_OPTION) {
				
				loadedFile = dialog.getSelectedFile();				
				
				// Make sure that it exists
				File fullPath = loadedFile.getAbsoluteFile();
				
				// Load
				if (fullPath.exists()) {
					try {
						load(fullPath.getPath());
						this.setTitle(fullPath.getName().replace(".txt", ""));
					}
					catch (IOException | NullPointerException | 
							NumberFormatException ex) {
						JOptionPane.showMessageDialog(null, 
								"Error with loading file.");
						ex.printStackTrace();
					}
				}
			}
		}
		
		else if (event.equals("Save")) {
			String save = JOptionPane.showInputDialog("File name?");
			save(SAVES_DIRECTORY+save);
			this.setTitle(save);
		}
		
		// Prepare track for playback
		else if (event.equals("Prepare")) {
			try {
				// Prepare tracks and create all essential files
				for (AudioTrack t : trackList) {
					int index = trackList.indexOf(t);
					t.setPlayOrder(patternList.get(index));	
				
					// Check every 10th of a second
					// for completion
					while (t.notDone) Thread.sleep(100); 
				}	
				
				// If we have a main track make sure it is ready
				if (mainTrackIndex > 0) {
					if (trackList.get(mainTrackIndex).track != null) {
						playButton.setEnabled(true);
					}
				}
				else {
					playButton.setEnabled(true);
				}
				prepareButton.setEnabled(false);
			}
			catch (Exception ex) {
				JOptionPane.showMessageDialog(null, 
						"Error with transposing files.");
				ex.printStackTrace();
			}	
		}
		
		// Start playback
		else if (event.equals("Play")) {				
			// We don't want people changing stuff while it runs
			file.setEnabled(false);
			edit.setEnabled(false);
			for (JButton b : importButtonList) b.setEnabled(false);
			for (JButton b : setPatternButtonList) b.setEnabled(false);
			for (JButton b : occButtonList) b.setEnabled(false);
			for (JButton b : recordButtonList) b.setEnabled(false);
			for (JRadioButton b : isMasterList) b.setEnabled(false);
			setLoopLength.setEnabled(false);
			
			// Reset tracks
			for (AudioTrack a : trackList) a.currentTrack = 0;
			currentList.replaceAll((i) -> 1);
		
			// Play tracks
			for (AudioTrack t : trackList) {
				t.play = playingList.get(trackList.indexOf(t)).isSelected();
				t.playNext(playButton);
			}
			
			// Start loop
			playManager.playTracks();
			
			// Change label and select playButton
			playButton.requestFocus();
			playButton.setText("End");
		} 
		
		// End playback
		else if (event.equals("End")) {
			
			// Stop loop
			playManager.controller.stop();
			
			for (AudioTrack t : trackList) {
				Iterator<Clip> i = t.audioInputStream.iterator();
				while (i.hasNext()) {
					i.next().stop();
					i.remove();
				}
			}
				
			//Enable Menu
			file.setEnabled(true);
			edit.setEnabled(true);
			
			// Re-enable buttons
			for (JButton b : importButtonList) b.setEnabled(true);
			for (JButton b : recordButtonList) b.setEnabled(true);
			
			for (int i = 0; i < trackNumber; i ++) {
				if (trackList.get(i).track != null) {
					setPatternButtonList.get(i).setEnabled(true);
					occButtonList.get(i).setEnabled(true);
					isMasterList.get(i).setEnabled(true);
				}
			}
			
			setLoopLength.setEnabled(true);
			
       		// Update displays
			for (int i = 0; i < trackNumber; i++) {
				countdownList.get(i).setText("Reps till next play: 0");
				nextToPlayList.get(i).setText("Next transposition: " 
						+ patternList.get(i).get(0));
			}
			
			playButton.setText("Play");
		}
		
		// Switch the main track
		else if (event.startsWith("Main")) {
			for (JRadioButton r : isMasterList) {
				int index = isMasterList.indexOf(r);
				
				// Set the main track index
								
				mainTrackIndex = eventIndex;
				
				if (trackList.get(eventIndex).track != null) {
					if (!prepareButton.isEnabled()) playButton.setEnabled(true);
						loopLength = calculateLength
							(trackList.get(eventIndex).trackDir);
				}
				
				// If the box isn't the one we picked, unselect it.
				if (index != eventIndex) { 
					r.setSelected(false);
					if (trackList.get(index).track != null) {
						occButtonList.get(index).setEnabled(true);
					}
				}
				
				// If it is, select it, disable it, and
				// reset reps and disable reps button
				// also, set the loop length
				else {
					r.setSelected(true);
					occButtonList.get(index).setEnabled(false);
					occList.get(index).setText("Rate of Occurrence: 1");
					maxList.set(index, 1);
					mainTrackIndex = index;
				}
				
				// Set button text in case the track was master before
				setLoopLength.setText("Set Loop Length (Current: " 
						+ loopFormat.format(loopLength) + ")");
			}
		}
		
		// Change the BPM of a file
		else if (event.equals("Change BPM of file")) {
			// Create a dialog to load a file
			JFileChooser dialog = 
					new JFileChooser(DIRECTORY);
			
			// Make sure the file is a wav
			FileNameExtensionFilter filter = 
					new FileNameExtensionFilter("*.wav", "wav");
			dialog.setFileFilter(filter);

			// Open the dialog
			int dialogOpen = dialog.showOpenDialog(this);
		    
			File loadedFile = null;
			
			// When we choose the file process it
			if(dialogOpen == JFileChooser.APPROVE_OPTION) {
				
				loadedFile = dialog.getSelectedFile();				
				// Make sure that it e
				
				File fullPath = loadedFile.getAbsoluteFile();
				
				// Try in case file does not exist
				// (we can't check without permissions)
				if (fullPath.exists()) {
					String length = JOptionPane.showInputDialog("Enter percent"
							+ "change");
					
					// Make sure it is > 0
					boolean pass = true;
					
					try {
						if (length != null) {
							if (Double.valueOf(length) < 0) {
								JOptionPane.showMessageDialog(null, 
										"Must be greater than 0.");
								pass = false;
							}
						}
						else {
							pass = false;
						}
					}
					catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(null, 
								"Please input a number.");
						pass = false;
					}
					if (pass) {
						// Calculate the change percentage
						double change = Double.valueOf(length);
					
						// Make sure it is in acceptable bounds
						if (change >= MIN_STRETCH 
								&& change <= MAX_STRETCH) {
							try {
								trackList.get(0).createShiftedFile( 
										change, fullPath.getPath());
							}
							catch (InterruptedException | IOException ex) {
								JOptionPane.showMessageDialog(null, 
										"Problem reading file.");
								ex.printStackTrace();
							}
						}
						else {
							JOptionPane.showMessageDialog(null, 
									"Value too high or low.");
						}
					}
				}
			}
		}
		
		// Set loop length
		else if (event.equals("Set Loop Length")) {
			String newLoop = JOptionPane.showInputDialog("Enter new "
					+ "length in seconds");
			
			// Make sure it is a float and > 0
			boolean pass = true;
			
			try {
				if (newLoop != null) {
					if (Double.valueOf(newLoop) < 0) {
						JOptionPane.showMessageDialog(null, 
								"Length must be greater than 0.");
						pass = false;
					}
				}
				else {
					pass = false;
				}
			}
			catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(null, 
						"Please input a number.");
				pass = false;
			}
			
			// Make sure it is only double
			if (pass) {	
				// Set loop length
				loopLength = Double.valueOf(newLoop);
				
				// Update button
				setLoopLength.setText("Set Loop Length (Current: " 
						+ loopFormat.format(loopLength) + ")");
				
				// Set radio button state (and occ button)
				if (mainTrackIndex != -1) {
					isMasterList.get(mainTrackIndex).setSelected(false);
					occButtonList.get(mainTrackIndex).setEnabled(true);
				}
				
				// Set button text
				setLoopLength.setText("Set Loop Length (Current: "
						+ loopFormat.format(loopLength) + ")");
				
				// Let the program know that there is no master track
				mainTrackIndex = -1;
			}
		}
		
		// Set track pattern
		else if (event.startsWith("Set Pattern")) {
			// Receive user input for pattern
			
			// Load the previous pattern into the textbox
			String patternString = "";
			if (patternList.get(eventIndex).size() > 0) {
				for (Object o : patternList.get(eventIndex)) {
					patternString += o + " ";
				}
			}
			
			String newPattern = JOptionPane.showInputDialog("Enter pattern, " 
					+ "separate integers with space. "
					+ "! for a break in the pattern, > to skip a loop."
					+ "and | to play at the same time",
					patternString);	
			
			// Make sure all input is acceptable
			boolean pass = true;
			String[] newPatternArray = null;
			
			if (newPattern != null) {
				newPatternArray = newPattern.split(" ");
				for (String s : newPatternArray) {
					try {
						if (!s.equals("!") && !s.equals(">") && 
								!s.equals("|")) {
							if (Integer.parseInt(s) < MIN_TRANSPOSE ||
								Integer.parseInt(s) > MAX_TRANSPOSE) {
								JOptionPane.showMessageDialog(null,
										"Please only input values between"
										+ "-60 and 60, or ! or > or |.");
								pass = false;
								break;
							}
						}
					}
					catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(null, 
								"Please input integers or ! or > or |"
								+ "separated by spaces.");
						pass = false;
						break;
					}
				}
			}
			else pass = false;
			
			// Set the pattern to the input 
			if (pass) {
				patternString = " "; // For display
				
				patternList.get(eventIndex).clear();
				
				for (String s : newPatternArray) {
					if (!s.equals("!") && !s.equals(">") && !s.equals("|")) {
						patternList.get(eventIndex).
							add(Integer.parseInt(s));
					}
					else {
						patternList.get(eventIndex).add(s);
					}
					patternString += s + " ";		
				}
				
				// Compact concurrent sequences
				patternString = patternString.replace(" | ", "|");
				
				// Set displays
				patternLabelList.get(eventIndex).setText("Pattern: " 
						+ patternString);
	        	nextToPlayList.get(eventIndex).setText("Next transposition: " 
						+ patternList.get(eventIndex).get(0));				
				
				playButton.setEnabled(false); // We need to re-prepare
				prepareButton.setEnabled(true);
			}
		}

		// Import file
		else if (event.startsWith("Import")) {
			// Create a dialog to load a file
			JFileChooser dialog = 
					new JFileChooser(DIRECTORY);
			
			// Make sure the file is a wav
			FileNameExtensionFilter filter = 
					new FileNameExtensionFilter("*.wav", "wav");
			dialog.setFileFilter(filter);

			// Open the dialog
			int dialogOpen = dialog.showOpenDialog(this);
		    
			File loadedFile = null;
			
			// When we choose the file process it
			if(dialogOpen == JFileChooser.APPROVE_OPTION) {
				
				loadedFile = dialog.getSelectedFile();				
				// Make sure that it e
				
				File fullPath = loadedFile.getAbsoluteFile();
				
				// Try in case file does not exist
				// (we can't check without permissions)
				if (fullPath.exists()) {

					trackList.get(eventIndex).setTrack(fullPath);
					
					if (eventIndex == mainTrackIndex) {
						loopLength = calculateLength
								(trackList.get(mainTrackIndex).trackDir);
					}
					
					// Modify button states
					playingList.get(eventIndex).setEnabled(true);
					playingList.get(eventIndex).setSelected(true);
					setPatternButtonList.get(eventIndex).setEnabled(true);
					
					// Set loop length text
					setLoopLength.setText("Set Loop Length (Current: " 
							+ loopFormat.format(loopLength) + ")");
					
					// Set all reps buttons to avaliable if this isn't
					// the main track
					occButtonList.get(eventIndex)
						.setEnabled(eventIndex != mainTrackIndex);
					
					isMasterList.get(eventIndex).setEnabled(true);
	
					// We need to prepare
					prepareButton.setEnabled(true);
					playButton.setEnabled(false); 
					
					// Set the title of the track
					trackBorderList.get(eventIndex).
						setTitle(loadedFile.getName());
					
					// Apply it
					revalidate();
					repaint();
				} 
				else {
					JOptionPane.showMessageDialog(null, 
							"File does not exist.");
				}
			}
		}
		
		// Start recording
		else if (event.startsWith("Record")) {
			if (!recording) {
				// Input Dialog
				recordingFile = JOptionPane.showInputDialog("What should the"
						+ " file be named?");
				
				if (recordingFile != null && recordingFile.length() > 0) {
					
					// Set recording state
					recording = true;

					// Seconds between countdown beeps
					double delay = loopLength;
					while (delay > .75) delay /= 2;
					
					// Choice var for option dialog
					choice = 1;
					boolean pass = false;
				
					// Is there a track other than main that can play?
					for (AudioTrack t: trackList) {
						if (t.track != null) {
							if (trackList.indexOf(t) != eventIndex) {
								pass = true;
							}
						}
					}
					
					if (pass && playButton.isEnabled()) {
						choice = JOptionPane.showOptionDialog
								(null, "Play the track in the background?",
								"Play the track in the background?",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.INFORMATION_MESSAGE,
								null,
								new Object[]{"Yes","No"},
								"No");
					}

					// Count in 
					try {					
						for (int i = 4; i > 0; i--) {
							// Delay
							Thread.sleep((long) (delay*1000));
				
							// Play a sound
							trackList.get(0).play(
									"resources/click.wav", playButton);	
						}	
				
						// One final delay
						Thread.sleep((long) (delay*1000));
				
					}
					catch (InterruptedException ex) {
						JOptionPane.showMessageDialog(null, 
								"Interrupted Exception.");
					};
					
					// Make a new thread and record with the first track
					new Thread(new Runnable() {
				           public void run() {
				             
				        	 // Record audio
				           	trackList.get(eventIndex).
				           		recordAudio(DIRECTORY
				           				+ recordingFile + EXTENTION);
				           }      
					}).start();
					
					// Modify record button state
					recordButtonList.get(eventIndex).setText("Stop");
					recordButtonList.get(eventIndex).setBackground(Color.RED);

					// Don't play the track we are recording on
					playingList.get(eventIndex).setSelected(false);
					
					// Start playback
					if (choice == 0) {
						playButton.doClick();
					}
					playButton.setEnabled(false);
					recordButtonList.get(eventIndex).setEnabled(true);
					for (JCheckBox c : playingList) c.setEnabled(false);
					
					// So we can stop it with space
					recordButtonList.get(eventIndex).requestFocus();
				}
			}
			else {
				
				// End recording state
				recording = false;

				// Stop the main track playback if we started it
				if (choice == 0) {
					playButton.setEnabled(true);
					playButton.doClick();
				}
				
				// Stop recording
				trackList.get(eventIndex).stopRecording();
				
				// Set file
				File fullPath = new File(DIRECTORY + recordingFile 
						+ EXTENTION);
				trackList.get(eventIndex).setTrack(fullPath);
			
				// Turn back into Record button
				recordButtonList.get(eventIndex).setText("Record");
				setPatternButtonList.get(eventIndex).setEnabled(true);
				recordButtonList.get(eventIndex).setBackground(Color.WHITE);
				

				// Modify track buttons
				playingList.get(eventIndex).setEnabled(true);
				playingList.get(eventIndex).setSelected(true);				
				
				// Modify button states			
				file.setEnabled(true);
				for (AudioTrack t : trackList) {
					int index = trackList.indexOf(t);
					
					importButtonList.get(index).setEnabled(true);
					recordButtonList.get(index).setEnabled(true);
	
					// Theses turn back on for all with tracks
					if (t.track != null) {
						isMasterList.get(index).setEnabled(true);
						setPatternButtonList.get(index).setEnabled(true);
						playingList.get(index).setEnabled(true);
						playingList.get(index).setSelected(true);
						// Don't enable for main track
						occButtonList.get(index).setEnabled(eventIndex 
								!= mainTrackIndex);
						
					}				
				}
				// this can now be the master list
				isMasterList.get(eventIndex).setEnabled(true);

				// We need to prepare
				prepareButton.setEnabled(true);
				playButton.setEnabled(false);
				
				// If this is the master track, set the loop duration to
				// its length
				if (eventIndex == mainTrackIndex) {
					loopLength = calculateLength
							(trackList.get(mainTrackIndex).trackDir);
				}
				
				setLoopLength.setText("Set Loop Length (Current: " 
						+ loopFormat.format(loopLength) + ")");
				
				// Update the name
				trackBorderList.get(eventIndex).setTitle(recordingFile 
						+ EXTENTION);
							
				// Apply it
				revalidate();
				repaint();
			}
		}
			
		// Set reps of track one per loop of track 2 3 and 4
		else if (event.startsWith("Set Rate of Occurrence")) {
			// Receive user input for pattern
			String newReps = JOptionPane.showInputDialog("Loops per rep");
			
			// Make sure it's an int greater than 0
			boolean pass = true;
			
			try {
				if (Integer.parseInt(newReps) <= 0) {
					JOptionPane.showMessageDialog(null, 
							"Please input an integer"
							+ " greater than 0.");
					pass = false;
				}
			}
			catch (NumberFormatException ex){
				JOptionPane.showMessageDialog(null, 
						"Please input an integer.");
				pass = false;
			}
			
			if (pass) { 
				// Get the last digit of the event and make it the index				
				maxList.set(eventIndex, Integer.parseInt(newReps));
				occList.get(eventIndex).setText("Rate of Occurrence: " 
						+ newReps);
			}
		}
		
		// Check to confirm 
		else if (event.equals("Quit")) {
			int relpy = JOptionPane.showConfirmDialog(null, 
					"Are you sure?",
					"Confirm",
					JOptionPane.YES_NO_OPTION);
			
			if (relpy == JOptionPane.YES_OPTION) {
				System.exit(-1);
			}
		}
		
		// In case of an unlisted event
		else {
			JOptionPane.showMessageDialog(null, "Bad input: " + event);
		}
	}
}