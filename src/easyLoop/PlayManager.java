/**
 * Sets a loop and regulates the playing of tracks
 * 
 * Part of the Easy Loop program
 * 
 * @author David Hampson (DavidHampson.97@gmail.com)
 * 
 * Pitch shifting and tempo changing made possible by "soundstretch"
 * @see http://www.surina.net/soundtouch/soundstretch.html
 */
package easyLoop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class PlayManager {
	
	ActionListener playTracks;
	Timer controller;
	
	/**
	 *  For reference
	 */
	EasyLoop easyLoop;
	
	/**
	 * Intializer
	 * 
	 * @param easyLoop EasyLoop for refrence
	 */
	public PlayManager (EasyLoop easyLoop) {
		this.easyLoop = easyLoop;
	}
	
	/**
	 * Drives the playing of tracks
	 */
	public void playTracks() {	
	    playTracks = new ActionListener() {
	    		public void actionPerformed(ActionEvent evt) {
	    			// Check to see if the conditions to play the tracks 
	        		// are met, and if they are play them. Also count 
	    			//towards next play for tracks with a non 1 loop count
	    			for (int i = 0; i < easyLoop.trackNumber; i ++){

	    				// if the box is checked we play
	    				easyLoop.trackList.get(i).play = 
	    						easyLoop.playingList.get(i).isSelected();
				
						// Is it time to play?
						if (easyLoop.maxList.get(i).
								equals(easyLoop.currentList.get(i))) {
							// Reset the counter
							
							easyLoop.currentList.set(i, 1);
							
							// Start playback
							easyLoop.trackList.get(i).
								playNext(easyLoop.playButton);
						}
	
						// if not, Increment the counter
						else {
							easyLoop.currentList.
								set(i, easyLoop.currentList.get(i)+1);
						}	    				        		

						// Update displays
	        			String s = String.valueOf
	        					(easyLoop.maxList.get(i) - 
	        							easyLoop.currentList.get(i));
	        			easyLoop.countdownList.get(i).
	        				setText("Reps till next play: " + s);
		        		
	 
	        			// Get the next to play for each track
	        			s = String.valueOf
	        					(easyLoop.patternList.get(i).
	        							get(easyLoop.trackList
	        									.get(i).currentTrack));
	        			easyLoop.nextToPlayList.get(i).
	        				setText("Next transposition: " + s);
	        	}
	    	}
	    };
	    
	    // Create the controller and set it to the file length
	    // (*1000 for seconds to milliseconds)
	    controller = new Timer((int) (easyLoop.loopLength * 
	    		1000), playTracks);
	    controller.start();
	}
}
