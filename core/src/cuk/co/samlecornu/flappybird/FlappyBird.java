package cuk.co.samlecornu.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

import sun.rmi.runtime.Log;

public class FlappyBird extends ApplicationAdapter {
	private SpriteBatch batch;
	private Random random_generator;
	private ShapeRenderer shape_renderer;
	//game background
	private Texture background;
	private Texture[] birds;
	//bird
	private int bird_sprite;
	private float bird_x;
	private float bird_y;
	private float bird_velocity;
	private Circle bird_hitbox;
	private int bird_sprite_counter;
	//tubes
	private Texture top_tube;
	private Texture bottom_tube;
	private float tube_gap;
	private float max_tube_offset;
	private float tube_velocity;
    private int number_of_tubes;
    private float[] tube_x;
    private float[] tube_offset;
    private float distance_between_tubes;
    private Rectangle[] top_tube_hitboxes;
	private Rectangle[] bottom_tube_hitboxes;
	private int score_tube;
	//gameover
	private Texture game_over;
	//game settings
	private int game_state;
	private float gravity;
	private float screen_height;
	private float screen_width;
	private  int score;
	private BitmapFont font;
	private GlyphLayout score_layout;
	@Override
	public void create () {
		//random gen
		random_generator = new Random();
		//drawers
		batch = new SpriteBatch();
		shape_renderer = new ShapeRenderer();
		//screen height and width
		screen_height = Gdx.graphics.getHeight();
		screen_width = Gdx.graphics.getWidth();
		Gdx.app.log("width", String.valueOf(screen_width));
		//background
		background = new Texture("bg.png");
		//bird
		birds = new Texture[2];
		birds[0] = new Texture("bird.png");
		birds[1] = new Texture("bird2.png");
		bird_sprite = 0;
		bird_sprite_counter = 0;
		bird_y = (screen_height/2) - (birds[bird_sprite].getHeight()/2);
		bird_x = (screen_width/2) - (birds[bird_sprite].getWidth()/2);
		bird_hitbox = new Circle();
		//tubes
		top_tube = new Texture("toptube.png");
		bottom_tube = new Texture("bottomtube.png");
		max_tube_offset = screen_height/2 - tube_gap/2 - 100;
		tube_velocity = 7;
        number_of_tubes = 10;
		tube_gap = 500;
        distance_between_tubes = screen_width/2;
        tube_x = new float[number_of_tubes];
        tube_offset = new float[number_of_tubes];
		top_tube_hitboxes = new Rectangle[number_of_tubes];
        bottom_tube_hitboxes = new Rectangle[number_of_tubes];
        for(int i =0; i < number_of_tubes; i++){
			tube_offset[i] = (random_generator.nextFloat() - 0.5f) * (screen_height - tube_gap - 200);
            tube_x[i] = screen_width/2 - top_tube.getWidth()/2 + screen_width +i * distance_between_tubes;
			top_tube_hitboxes[i] = new Rectangle();
			bottom_tube_hitboxes[i] = new Rectangle();
        }
        //gameover
		game_over = new Texture("gameover.png");
		//game settings
		game_state = 0;
		gravity = 2;
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/myfont.TTF"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		font =  generator.generateFont(parameter);
		parameter.size = 12;
		font.setColor(Color.WHITE);

		font.getData().setScale(7);
		score_layout= new GlyphLayout();
		startGame();
	}
    private void startGame(){
        score = 0;
        score_tube = 0;
        bird_velocity = 0;
		score_layout.setText(font,String.valueOf(score));
        bird_y = (screen_height/2) - (birds[bird_sprite].getHeight()/2);
        for(int i =0; i < number_of_tubes; i++){
            tube_offset[i] = (random_generator.nextFloat() - 0.5f) * (screen_height - tube_gap - 200);
            tube_x[i] = screen_width/2 - top_tube.getWidth()/2 + screen_width +i * distance_between_tubes;
            top_tube_hitboxes[i] = new Rectangle();
            bottom_tube_hitboxes[i] = new Rectangle();
        }
    }

	@Override
	public void render (){

		batch.begin();
		//draw background
		batch.draw(background, 0, 0, screen_width, screen_height);

		if(game_state == 1){
			if(tube_x[score_tube] < screen_width/2){
				score++;
				if(++score_tube >= number_of_tubes){
					score_tube = 0;
				}
			}
			if(Gdx.input.justTouched()) {
				bird_velocity = -30;
			}

            //draw tubes
            for(int i =0; i < number_of_tubes; i++){
            	if(tube_x[i] < -top_tube.getWidth()){
            		tube_x[i] += number_of_tubes * distance_between_tubes;
					tube_offset[i] = (random_generator.nextFloat() - 0.5f) * (screen_height - tube_gap - 200);
				}else{
					tube_x[i] -= tube_velocity;
            	}
                batch.draw(top_tube, tube_x[i], screen_height/2 + tube_gap/2 + tube_offset[i]);
                batch.draw(bottom_tube, tube_x[i], screen_height/2 - tube_gap/2 - bottom_tube.getHeight() + tube_offset[i]);

                top_tube_hitboxes[i] = new Rectangle(tube_x[i], screen_height/2 + tube_gap/2 + tube_offset[i], top_tube.getWidth(), top_tube.getHeight());
				bottom_tube_hitboxes[i] = new Rectangle(tube_x[i], screen_height/2 - tube_gap/2 - bottom_tube.getHeight() + tube_offset[i], bottom_tube.getWidth(), bottom_tube.getHeight());
            }
			//check if bird is on screen
			if(bird_y > 0 && bird_y < screen_height){
				bird_velocity += gravity;
				bird_y -= bird_velocity;
			}else{
				//if not game over
				game_state = 2;
			}
		}else if(game_state ==0){
			if (Gdx.input.justTouched()) {
				game_state = 1;
			}
		}else if(game_state == 2){
			batch.draw(game_over, screen_width/2 - game_over.getWidth()/2, screen_height/2 - game_over.getHeight()/2);
			if(Gdx.input.justTouched()){
				game_state = 1;
				startGame();
			}
		}

		//draw bird and hitbox
		batch.draw(birds[bird_sprite], bird_x, bird_y);

		font.draw(batch,String.valueOf(score), (screen_width/2) - score_layout.width, screen_height - (screen_height/12));
		batch.end();
//		shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
//		shape_renderer.setColor(Color.RED);
		bird_hitbox.set(screen_width/2, bird_y + birds[bird_sprite].getHeight()/2, birds[bird_sprite].getWidth()/2);
//		shape_renderer.circle(bird_hitbox.x, bird_hitbox.y, bird_hitbox.radius);

        //draw tube hitbox
        for(int i =0; i < number_of_tubes; i++){
//            shape_renderer.rect(tube_x[i], screen_height/2 + tube_gap/2 + tube_offset[i], top_tube.getWidth(), top_tube.getHeight());
//            shape_renderer.rect(tube_x[i], screen_height/2 - tube_gap/2 - bottom_tube.getHeight() + tube_offset[i], bottom_tube.getWidth(), bottom_tube.getHeight());

            //Collision detection
            if(Intersector.overlaps(bird_hitbox,top_tube_hitboxes[i]) || Intersector.overlaps(bird_hitbox,bottom_tube_hitboxes[i])){
				game_state = 2;
			}
        }
//        shape_renderer.end();

        //update bird sprite

		if(++bird_sprite_counter % 3 == 0){
			bird_sprite = (bird_sprite == 1) ? 0: 1;
		}
	}

	@Override
	public void dispose () {

	}
}
