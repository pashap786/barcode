package barcode;

import java.util.Scanner;

public class PauseTest {
	
	
	
	public static void main(String[] args) {
	
	int	turns = 0;
	int turncount = 0;
		boolean paused = false;
		while(turncount < 100) {
			if(!paused) {
				turncount = turncount+1;
				System.out.println("IM ON THIS TURN "+turncount);
				Scanner s = new Scanner(System.in);
				String pausein = s.nextLine();
				if(pausein.equalsIgnoreCase("y")) {
					paused = true;
				} else {
					paused = false;
				}
			}
		}
		
	}

}
