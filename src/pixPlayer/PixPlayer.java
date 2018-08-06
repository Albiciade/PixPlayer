package pixPlayer;

import processing.core.*;
import processing.data.XML;
import ddf.minim.*;

import java.io.*;

import com.mpatric.mp3agic.*;

public class PixPlayer extends PApplet {

	float sizePercentage = 0.8f;
	int windowWidth, windowHeight;

	int textSize = 100;
	PImage albumCover, background;
	public Button[] buttons;
	public int buttonSize;

	String songName;
	String album;
	String artist;

	float tl, pl, tsecs, psecs;
	int tminutes, pminutes;
	float mp;

	float textSizeAlbumInfos = 1;

	Minim minim;
	public static AudioPlayer song;

	Mp3File songmp3;
	ID3v2 meta;

	PFont montserrat, montserratBold;

	XML xml;

	public static void main(String[] args) {
		PApplet.main(PixPlayer.class.getName());
	}

	public void settings() {
		int windowWidth = (int) (sizePercentage * displayWidth);
		int windowHeight = (int) (sizePercentage * displayHeight);

		size(windowWidth, windowHeight);
	}

	public void setup() {
		frameRate(200);
		smooth();
		buttonSize = (int) (height / 12);

		buttons = new Button[4];
		buttons[0] = new Button(this, (float) (0.625 * width - 0.5 * buttonSize), (float) (0.1 * height + 0.46 * width),
				buttonSize, "play-button.svg", "play");
		buttons[1] = new Button(this, (float) (0.625 * width + 0.6 * buttonSize), (float) (0.1 * height + 0.46 * width),
				buttonSize, "next-button.svg", "next");
		buttons[2] = new Button(this, (float) (0.625 * width - 1.6 * buttonSize), (float) (0.1 * height + 0.46 * width),
				buttonSize, "previous-button.svg", "prev");
		buttons[3] = new Button(this, (float) (width - buttonSize / 1.5), 10, (float) (buttonSize / 1.5), "song.svg",
				"song");

		background = loadImage("background.jpg");
		albumCover = loadImage("album-art.png");

		background.resize(width, height);
		albumCover.resize((int) (0.45 * width), (int) (0.45 * width));

		montserrat = createFont("Montserrat-Regular.ttf", 48);
		montserratBold = createFont("Montserrat-Bold.ttf", 48);
		minim = new Minim(this);
	}

	public void draw() {

		displayWindow();
		updatePlayer();

		/*
		 * if (song != null) {
		 * 
		 * if (textWidth("Artiste(s) : " + artist) < 0.25 * width &&
		 * textWidth("Titre : " + songName) < 0.25 * width && textWidth("Album : " +
		 * album) < 0.25 * width) { textSizeAlbumInfos++; } }
		 */
	}

	public void stop() {
		song.close();
		super.stop();

	}

	public void mousePressed() {
		for (int i = 0; i < buttons.length; i++) {
			if (mouseX > buttons[i].x && mouseX < buttons[i].x + buttons[i].s && mouseY > buttons[i].y
					&& mouseY < buttons[i].y + buttons[i].s) {
				buttons[i].action();
			}
		}

		if (mouseX > 0.4 * width && mouseX < 0.85 * width && mouseY > 0.91 * height && mouseY < 0.93 * height) {
			if (song != null) {
				float x = map(mouseX, (float) (0.4 * width), (float) (0.85 * width), 0, song.length());
				song.cue((int) (x));
			}
		}
	}

	private void updatePlayer() {
		if (song != null) {
			if (song.position() >= song.length() - 50) {
				song.pause();
				song.cue(0);
				song.play();
			}

			if (song.isPlaying()) {
				buttons[0].shape = loadShape("pause-button.svg");
			} else {
				buttons[0].shape = loadShape("play-button.svg");
			}
		}
	}

	private void displayWindow() {

		background(120);
		image(background, 0, 0);
		image(albumCover, (float) (0.4 * width), (float) (0.1 * height));

		for (int i = 0; i < buttons.length; i++) {
			buttons[i].display();
		}

		stroke(0);
		strokeWeight(10);
		line((float) (0.4 * width), (float) (0.92 * height), (float) (0.85 * width), (float) (0.92 * height));

		if (song != null) {

			textSize(textSize);
			textFont(montserratBold);
			textAlign(CENTER, CENTER);
			fill(255);

			text(songName, (float) (0.625 * width), (float) (0.03 * height));

			textFont(montserrat);

			text(artist, (float) (0.625 * width), (float) (0.07 * height));

			textAlign(LEFT);

			textSize(textSize);
			text("Artiste(s) : " + artist, (float) (width * 0.05), (float) (height * 0.15));
			text("Titre : " + songName, (float) (width * 0.05), (float) (height * 0.2));
			text("Album : " + album, (float) (width * 0.05), (float) (height * 0.25));

			textAlign(CENTER, CENTER);

			stroke(136, 205, 250);
			mp = map(song.position(), 0, song.length(), (float) (0.4 * width), (float) (0.85 * width));
			line((float) (0.4 * width), (float) (0.92 * height), mp, (float) (0.92 * height));

			pl = song.position() / 1000;
			pminutes = floor(pl / 60);
			psecs = pl % 60;

			textAlign(CENTER);
			textSize(20);
			fill(255);
			text(nf(tminutes, 2, 0) + ":" + nf(tsecs, 2, 0), (float) (0.85 * width), (float) (0.95 * height));
			text(nf(pminutes, 2, 0) + ":" + nf(psecs, 2, 0), (float) (0.4 * width), (float) (0.95 * height));
		}
	}

	public void selectionMusique(File music) {
		if (music == null) {
			println("Fenêtre fermée.");
		} else {
			String p = music.getAbsolutePath();
			if (p.endsWith(".mp3")) {
				if (song != null) {
					song.pause();
					song.cue(0);
				}
				textSizeAlbumInfos = 1;
				String[] name = split(p, '\\');

				song = minim.loadFile(p);

				tl = song.length() / 1000;
				tminutes = floor(tl / 60);
				tsecs = tl % 60;

				try {
					songmp3 = new Mp3File(p);
				} catch (IOException e) {
					e.printStackTrace();
					exit();
				} catch (UnsupportedTagException e) {
					e.printStackTrace();
					exit();
				} catch (InvalidDataException e) {
					e.printStackTrace();
					exit();
				}

				meta = songmp3.getId3v2Tag();

				if (meta.getTitle() == null || meta.getTitle() == "") {
					songName = name[name.length - 1];
					songName = songName.replace(".mp3", "");
				} else {
					songName = meta.getTitle();
				}

				if (meta.getArtist() == null || meta.getArtist() == "") {
					artist = "Inconnu";
				} else {
					artist = meta.getArtist();
				}
				
				textSize(textSize);
				while (textWidth("Artiste(s) : " + artist) > 0.4 * width - 0.05 * width) {
					textSize--;
					textSize(textSize);
				}
				
				String[] artists = split(artist, '/');

				if (meta.getAlbum() == null || meta.getAlbum() == "") {
					if (meta.getTitle().equals(songName) && meta.getArtist().equals(artist)) {
						xml = loadXML(
								"http://ws.audioscrobbler.com/2.0/?method=track.getinfo&api_key=6dc252b099b960e1db56034b4bc770b3&artist="
										+ artists[0].trim().replace("é", "%C3%A9").replace(" ", "+") + "&track="
										+ songName.trim().replace(" ", "+").replace("é", "%C3%A9")
												.replace("Ç", "%C3%87").replace("'", "%27"));
						XML[] tracks = xml.getChildren("track");
						if (tracks != null) {
							XML[] albums = tracks[0].getChildren("album");
							if (albums != null) {
								XML[] titles = albums[0].getChildren("title");
								String title = titles[0].getContent();

								if (title == null || title == "") {
									album = "Inconnu";
								} else {
									album = title;
								}
							}
						}
					} else {
						album = "Inconnu";
					}
				} else {
					album = meta.getAlbum();
				}

				xml = loadXML(
						"http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=6dc252b099b960e1db56034b4bc770b3&artist="
								+ artists[0].trim().replace("é", "%C3%A9").replace(" ", "+") + "&album="
								+ album.trim().replace(" ", "+").replace("é", "%C3%A9") + "&autocorrect=1");
				XML[] albums = xml.getChildren("album");
				XML[] images = albums[0].getChildren("image");
				String link = images[4].getContent();
				PImage dummycover = loadImage(link);
				if (dummycover != null) {
					albumCover = loadImage(link);
					albumCover.resize((int) (0.45 * width), (int) (0.45 * width));
				}
			}
		}
		/*
		 * text("Artiste(s) : " + artist, (float) (width * 0.05), (float) (height *
		 * 0.15)); text("Titre : " + songName, (float) (width * 0.05), (float) (height *
		 * 0.2)); text("Album : " + album, (float) (width * 0.05), (float) (height *
		 * 0.25));
		 */
	}
}