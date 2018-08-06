package pixPlayer;
import processing.core.*;

public class Button {

	PApplet p;
	float x, y, s;
	PShape shape;
	String id;

	Button(PApplet parent, float xpos, float ypos, float size, String name, String ids) {
		p = parent;
		x = xpos;
		y = ypos;
		s = size;
		shape = p.loadShape(name);
		id = ids;
	}

	void display() {
		p.shape(shape, x, y, s, s);
	}

	void action() {
		switch (id) {
		case "play":
			if (PixPlayer.song != null) {
				if (PixPlayer.song.isPlaying()) {
					PixPlayer.song.pause();
				} else {
					PixPlayer.song.play();
				}
			}
			break;

		case "song":

			p.selectInput("Sélectionnez votre fichier", "selectionMusique");
			break;

		case "prev":

			if (PixPlayer.song != null) {
				PixPlayer.song.pause();
				PixPlayer.song.cue(0);
				PixPlayer.song.play();
			}
			break;

		default:
			break;
		}
	}
}