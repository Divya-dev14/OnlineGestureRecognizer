package recognizer;

public class GestureScore {
	String name;
	double score;

	public GestureScore(String gesture_name, double gesture_score) {
		this.name = gesture_name;
		this.score = gesture_score;
	}

	@Override
	public String toString(){
		return "Recognized: "+ name + "; Score: " + (score+1);
	}
}
