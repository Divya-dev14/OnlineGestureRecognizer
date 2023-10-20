
package recognizer;
    
        import javax.swing.JLabel;
	import javax.swing.JPanel;
	import javax.swing.SwingUtilities;
        import java.awt.BasicStroke;
	import java.awt.Color;
	import java.awt.Dimension;
	import java.awt.Graphics;
	import java.awt.Graphics2D;
	import java.awt.Shape;
	import java.awt.event.MouseAdapter;
	import java.awt.event.MouseEvent;
	import java.awt.geom.Path2D;
	import java.util.ArrayList;
	import javax.swing.JFrame;



	public class CanvasBoard extends JPanel {

        //local variables

	private static final long serialVersionUID = 1L;

	private GesturePoint initial_point;
	private GesturePoint end_point;
	private Shape shape;
	private static Dimension board_size = new Dimension(600, 600);

        // Jlabel & Jframe objects
	private static JLabel label_resampled;
	private static JFrame frame;
	private static JLabel label_template;
	private static JLabel label_template2;

          //data structure to hold templates points
	private static ArrayList<GesturePoint> temp_points = new ArrayList<>();

            // these varaibles are needed for calculations
	    public static double theta = Math.toRadians(45.0);
	    public static double dtheta = Math.toRadians(2.0);
	    public static double maxSize = 250.0;
	    public static int N = 64;
	    public static double diagonal = Math.sqrt(Math.pow(maxSize, 2) + Math.pow(maxSize, 2));
	    public static double half = 0.5*diagonal;
	    public static double phi = 0.5 * (Math.sqrt(5.0)-1.0);


            //data structure to hold the points drawn by user
	    private static ArrayList<GesturePoint> points = new ArrayList<GesturePoint>();
	    private static ArrayList<GesturePoint> prev;
            //data structure to hold templates
	    public static ArrayList<GestureTemplate> templates = new ArrayList<GestureTemplate>();

            //canvas board constructor
	    public CanvasBoard() {

                //setting the background to be white
	        setBackground(Color.white);
                //setting up the area size
	        setPreferredSize(board_size);

	        PathListener listener = new PathListener();
                //adding mouse events
	        addMouseListener(listener);
	        addMouseMotionListener(listener);
	    }

            //This will set up the brush color to be blue & setups graphics object
	    public void paintComponent(Graphics obj1) {
	        super.paintComponent(obj1);

	        Graphics2D obj2 = (Graphics2D) obj1;

	        if (initial_point != null && end_point != null) {
	            BasicStroke stroke = new BasicStroke(1);
	            Shape strokedShape = stroke.createStrokedShape(shape);
	            obj2.setPaint(Color.BLUE);
	            obj2.draw(strokedShape);
	            obj2.fill(strokedShape);
	    		repaint();
	        }
	    }

            //mousePressed method, this will add points drawn to an arrayList

	    private class PathListener extends MouseAdapter {
	        public void mousePressed(MouseEvent e) {

	        	int current_x = e.getPoint().x;
	        	int current_y = e.getPoint().y;

                    // this is the start point
	            initial_point = new GesturePoint(current_x,current_y);

                    //adding start point to the arraylist
	            points.add(initial_point);
	            Path2D way = new Path2D.Double();
	            shape = way;
	        }

                //identifies mouseDrag event & this will add points drawn to an arrayList
	        public void mouseDragged(MouseEvent eve) {

	            end_point = new GesturePoint(eve.getPoint().x, eve.getPoint().y);

                     //adding point to the arraylist
	            points.add(end_point);

	            Path2D way = (Path2D) shape;
	            way.moveTo(initial_point.x, initial_point.y);
	            way.lineTo(end_point.x, end_point.y);
	            shape = way;
	            initial_point = end_point;
	            repaint();
	        }

                //identifies mouseRelease event & this will send array of points to process further

	        public void mouseReleased(MouseEvent event) {
	            Path2D way = (Path2D) shape;

                    //try ctach block
	            try
                    {
	                way.closePath();
	            } catch(Exception ingore) {
	            }

	            shape = way;

                    //repainting
	            repaint();

	            if(points.size() != 0){
	            	prev = points;
                        //processing the points
	            	helper_method2(prev);
	            }

                   //clearing the arraylist of points
	          points.clear();
	        }
	    }

            //contains all 16 gesture shapes
	    public static void outputShapes(){
                //16 gestures are maintained in 2 below labels as shown here
                //this will be displayed on canvas board, to let user know to draw these gestures
	    	label_template.setText("Triangle, Rectangle, Circle, Caret, Arrow, Delete, PigTail, X");
	    	label_template2.setText("V, Right curly brace, Left curly brace, Right square bracket, Left square bracket, Star, Check, Zig-Zag");

		}

            //calls the menthods resampling, rotation,scaling,translation internally

	    public ArrayList<GesturePoint> preRecognize(ArrayList<GesturePoint> points){
			ArrayList<GesturePoint> points_list = points;

                        //sending points to resampling method
			points_list = GestureComputing.resampling(points_list, N);

			double a = 0.0;
			double b = 0.0;

			for(int i=0; i<points.size(); i++)
                        {
				GesturePoint cur = points.get(i);
				a +=cur.x;
				b += cur.y;
			}

			double newx = a / (double)points.size();
			double newy = b / (double)points.size();

			GesturePoint middle = new GesturePoint(newx, newy);
			GesturePoint cur_pt = points.get(0);

			double angleValue = Math.atan2(middle.y - cur_pt.y, middle.x - cur_pt.x);

                        //sending points to rotation method
			points_list = GestureComputing.rotation(points_list, -1.0*angleValue);
                        //sending points to scaling method
			points_list = GestureComputing.scaling(points_list, maxSize);
                        //sending points to translation method
			points_list = GestureComputing.translation(points_list, new GesturePoint(0,0));

			return points_list;
		}

            //method to call prerecognize and recognize methods
	    public void helper_method2(ArrayList<GesturePoint> points) {

                //calling prerecognize method
	    	ArrayList<GesturePoint> points2 = preRecognize(points);
                //calling recognize method
	    	GestureScore score = GestureComputing.recognizing(points2);
                //adding the score to label
	    	label_resampled.setText(""+score);
	    }

              //execution starts from here
	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(new Runnable() {

	            public void run() {

                        //instantiation of Jlabel field
                        //to hold the resampled gesture name and score
	            	label_resampled = new JLabel();
			label_template = new JLabel();
			label_template2 = new JLabel();

                        //method to load all 16 gesture templates
	            	readGestureTemplates();

                         //contains all 16 gesture shapes
	            	outputShapes();

                        //CanvasBoard instantiation
	                CanvasBoard area = new CanvasBoard();

                        //setting the frame object
	                frame = new JFrame("Canvas $1 Recognizer");
	                frame.setResizable(false);

                        //setting the JPanel object
	                JPanel panelObject = new JPanel();
	                panelObject.setPreferredSize(new Dimension(board_size.width+60, board_size.height+120));
	                JPanel controlPanel = new JPanel();

                        //adding the labels to panel object
	                panelObject.add(area, "wrap");
			panelObject.add(label_template, "wrap");
			panelObject.add(label_template2, "wrap");
			panelObject.add(label_resampled, "wrap");
                        panelObject.add(controlPanel);

                        //default close operation
	                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	                frame.getContentPane().add(panelObject);

	                frame.pack();
	                frame.setVisible(true);
	            }
	        });
	    }


                //This method keep tracks of loaded gesture templates
                public static void add_template(GestureTemplate template) {
			templates.add(template);
		}

                //This method adds points to each template
		public static void add_templatePoints(GesturePoint... point) {
			for(GesturePoint p : point) {
				temp_points.add(p);
			}

		}

                //Below code has been taken from dollar.js from (http://depts.washington.edu/acelab/proj/dollar/index.html)
                //changed to java version
                //contains points of 16 template gestures

                public static void readGestureTemplates() {

	    	temp_points = new ArrayList<>();
                        //adding points for triangle
                        add_templatePoints(new GesturePoint(137,139),new GesturePoint(135,141),new GesturePoint(133,144),new GesturePoint(132,146),new GesturePoint(130,149),new GesturePoint(128,151),new GesturePoint(126,155),new GesturePoint(123,160),new GesturePoint(120,166),new GesturePoint(116,171),new GesturePoint(112,177),new GesturePoint(107,183),new GesturePoint(102,188),new GesturePoint(100,191),new GesturePoint(95,195),new GesturePoint(90,199),new GesturePoint(86,203),new GesturePoint(82,206),new GesturePoint(80,209),new GesturePoint(75,213),new GesturePoint(73,213),new GesturePoint(70,216),new GesturePoint(67,219),new GesturePoint(64,221),new GesturePoint(61,223),new GesturePoint(60,225),new GesturePoint(62,226),new GesturePoint(65,225),new GesturePoint(67,226),new GesturePoint(74,226),new GesturePoint(77,227),new GesturePoint(85,229),new GesturePoint(91,230),new GesturePoint(99,231),new GesturePoint(108,232),new GesturePoint(116,233),new GesturePoint(125,233),new GesturePoint(134,234),new GesturePoint(145,233),new GesturePoint(153,232),new GesturePoint(160,233),new GesturePoint(170,234),new GesturePoint(177,235),new GesturePoint(179,236),new GesturePoint(186,237),new GesturePoint(193,238),new GesturePoint(198,239),new GesturePoint(200,237),new GesturePoint(202,239),new GesturePoint(204,238),new GesturePoint(206,234),new GesturePoint(205,230),new GesturePoint(202,222),new GesturePoint(197,216),new GesturePoint(192,207),new GesturePoint(186,198),new GesturePoint(179,189),new GesturePoint(174,183),new GesturePoint(170,178),new GesturePoint(164,171),new GesturePoint(161,168),new GesturePoint(154,160),new GesturePoint(148,155),new GesturePoint(143,150),new GesturePoint(138,148),new GesturePoint(136,148));
			GestureTemplate triangle = new GestureTemplate("Triangle", temp_points);
			add_template(triangle);

                        //adding points for x
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(87,142),new GesturePoint(89,145),new GesturePoint(91,148),new GesturePoint(93,151),new GesturePoint(96,155),new GesturePoint(98,157),new GesturePoint(100,160),new GesturePoint(102,162),new GesturePoint(106,167),new GesturePoint(108,169),new GesturePoint(110,171),new GesturePoint(115,177),new GesturePoint(119,183),new GesturePoint(123,189),new GesturePoint(127,193),new GesturePoint(129,196),new GesturePoint(133,200),new GesturePoint(137,206),new GesturePoint(140,209),new GesturePoint(143,212),new GesturePoint(146,215),new GesturePoint(151,220),new GesturePoint(153,222),new GesturePoint(155,223),new GesturePoint(157,225),new GesturePoint(158,223),new GesturePoint(157,218),new GesturePoint(155,211),new GesturePoint(154,208),new GesturePoint(152,200),new GesturePoint(150,189),new GesturePoint(148,179),new GesturePoint(147,170),new GesturePoint(147,158),new GesturePoint(147,148),new GesturePoint(147,141),new GesturePoint(147,136),new GesturePoint(144,135),new GesturePoint(142,137),new GesturePoint(140,139),new GesturePoint(135,145),new GesturePoint(131,152),new GesturePoint(124,163),new GesturePoint(116,177),new GesturePoint(108,191),new GesturePoint(100,206),new GesturePoint(94,217),new GesturePoint(91,222),new GesturePoint(89,225),new GesturePoint(87,226),new GesturePoint(87,224));
			GestureTemplate xtemp = new GestureTemplate("x", temp_points);
			add_template(xtemp);

                        //adding points for rectangle
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(78,149),new GesturePoint(78,153),new GesturePoint(78,157),new GesturePoint(78,160),new GesturePoint(79,162),new GesturePoint(79,164),new GesturePoint(79,167),new GesturePoint(79,169),new GesturePoint(79,173),new GesturePoint(79,178),new GesturePoint(79,183),new GesturePoint(80,189),new GesturePoint(80,193),new GesturePoint(80,198),new GesturePoint(80,202),new GesturePoint(81,208),new GesturePoint(81,210),new GesturePoint(81,216),new GesturePoint(82,222),new GesturePoint(82,224),new GesturePoint(82,227),new GesturePoint(83,229),new GesturePoint(83,231),new GesturePoint(85,230),new GesturePoint(88,232),new GesturePoint(90,233),new GesturePoint(92,232),new GesturePoint(94,233),new GesturePoint(99,232),new GesturePoint(102,233),new GesturePoint(106,233),new GesturePoint(109,234),new GesturePoint(117,235),new GesturePoint(123,236),new GesturePoint(126,236),new GesturePoint(135,237),new GesturePoint(142,238),new GesturePoint(145,238),new GesturePoint(152,238),new GesturePoint(154,239),new GesturePoint(165,238),new GesturePoint(174,237),new GesturePoint(179,236),new GesturePoint(186,235),new GesturePoint(191,235),new GesturePoint(195,233),new GesturePoint(197,233),new GesturePoint(200,233),new GesturePoint(201,235),new GesturePoint(201,233),new GesturePoint(199,231),new GesturePoint(198,226),new GesturePoint(198,220),new GesturePoint(196,207),new GesturePoint(195,195),new GesturePoint(195,181),new GesturePoint(195,173),new GesturePoint(195,163),new GesturePoint(194,155),new GesturePoint(192,145),new GesturePoint(192,143),new GesturePoint(192,138),new GesturePoint(191,135),new GesturePoint(191,133),new GesturePoint(191,130),new GesturePoint(190,128),new GesturePoint(188,129),new GesturePoint(186,129),new GesturePoint(181,132),new GesturePoint(173,131),new GesturePoint(162,131),new GesturePoint(151,132),new GesturePoint(149,132),new GesturePoint(138,132),new GesturePoint(136,132),new GesturePoint(122,131),new GesturePoint(120,131),new GesturePoint(109,130),new GesturePoint(107,130),new GesturePoint(90,132),new GesturePoint(81,133),new GesturePoint(76,133));
			GestureTemplate rectangle = new GestureTemplate("rectangle", temp_points);
			add_template(rectangle);

                        //adding points for circle
	          	temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(127,141),new GesturePoint(124,140),new GesturePoint(120,139),new GesturePoint(118,139),new GesturePoint(116,139),new GesturePoint(111,140),new GesturePoint(109,141),new GesturePoint(104,144),new GesturePoint(100,147),new GesturePoint(96,152),new GesturePoint(93,157),new GesturePoint(90,163),new GesturePoint(87,169),new GesturePoint(85,175),new GesturePoint(83,181),new GesturePoint(82,190),new GesturePoint(82,195),new GesturePoint(83,200),new GesturePoint(84,205),new GesturePoint(88,213),new GesturePoint(91,216),new GesturePoint(96,219),new GesturePoint(103,222),new GesturePoint(108,224),new GesturePoint(111,224),new GesturePoint(120,224),new GesturePoint(133,223),new GesturePoint(142,222),new GesturePoint(152,218),new GesturePoint(160,214),new GesturePoint(167,210),new GesturePoint(173,204),new GesturePoint(178,198),new GesturePoint(179,196),new GesturePoint(182,188),new GesturePoint(182,177),new GesturePoint(178,167),new GesturePoint(170,150),new GesturePoint(163,138),new GesturePoint(152,130),new GesturePoint(143,129),new GesturePoint(140,131),new GesturePoint(129,136),new GesturePoint(126,139));
			GestureTemplate circle = new GestureTemplate("circle", temp_points);
			add_template(circle);

                        //adding points for check
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(91,185),new GesturePoint(93,185),new GesturePoint(95,185),new GesturePoint(97,185),new GesturePoint(100,188),new GesturePoint(102,189),new GesturePoint(104,190),new GesturePoint(106,193),new GesturePoint(108,195),new GesturePoint(110,198),new GesturePoint(112,201),new GesturePoint(114,204),new GesturePoint(115,207),new GesturePoint(117,210),new GesturePoint(118,212),new GesturePoint(120,214),new GesturePoint(121,217),new GesturePoint(122,219),new GesturePoint(123,222),new GesturePoint(124,224),new GesturePoint(126,226),new GesturePoint(127,229),new GesturePoint(129,231),new GesturePoint(130,233),new GesturePoint(129,231),new GesturePoint(129,228),new GesturePoint(129,226),new GesturePoint(129,224),new GesturePoint(129,221),new GesturePoint(129,218),new GesturePoint(129,212),new GesturePoint(129,208),new GesturePoint(130,198),new GesturePoint(132,189),new GesturePoint(134,182),new GesturePoint(137,173),new GesturePoint(143,164),new GesturePoint(147,157),new GesturePoint(151,151),new GesturePoint(155,144),new GesturePoint(161,137),new GesturePoint(165,131),new GesturePoint(171,122),new GesturePoint(174,118),new GesturePoint(176,114),new GesturePoint(177,112),new GesturePoint(177,114),new GesturePoint(175,116),new GesturePoint(173,118));
			GestureTemplate check = new GestureTemplate("check", temp_points);
			add_template(check);

                        //adding points for caret
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(79,245),new GesturePoint(79,242),new GesturePoint(79,239),new GesturePoint(80,237),new GesturePoint(80,234),new GesturePoint(81,232),new GesturePoint(82,230),new GesturePoint(84,224),new GesturePoint(86,220),new GesturePoint(86,218),new GesturePoint(87,216),new GesturePoint(88,213),new GesturePoint(90,207),new GesturePoint(91,202),new GesturePoint(92,200),new GesturePoint(93,194),new GesturePoint(94,192),new GesturePoint(96,189),new GesturePoint(97,186),new GesturePoint(100,179),new GesturePoint(102,173),new GesturePoint(105,165),new GesturePoint(107,160),new GesturePoint(109,158),new GesturePoint(112,151),new GesturePoint(115,144),new GesturePoint(117,139),new GesturePoint(119,136),new GesturePoint(119,134),new GesturePoint(120,132),new GesturePoint(121,129),new GesturePoint(122,127),new GesturePoint(124,125),new GesturePoint(126,124),new GesturePoint(129,125),new GesturePoint(131,127),new GesturePoint(132,130),new GesturePoint(136,139),new GesturePoint(141,154),new GesturePoint(145,166),new GesturePoint(151,182),new GesturePoint(156,193),new GesturePoint(157,196),new GesturePoint(161,209),new GesturePoint(162,211),new GesturePoint(167,223),new GesturePoint(169,229),new GesturePoint(170,231),new GesturePoint(173,237),new GesturePoint(176,242),new GesturePoint(177,244),new GesturePoint(179,250),new GesturePoint(181,255),new GesturePoint(182,257));
			GestureTemplate caret = new GestureTemplate("caret", temp_points);
			add_template(caret);

                        //adding points for zigzag
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(307,216),new GesturePoint(333,186),new GesturePoint(356,215),new GesturePoint(375,186),new GesturePoint(399,216),new GesturePoint(418,186));
			GestureTemplate zig = new GestureTemplate("zig-zag", temp_points);
			add_template(zig);

                        //adding points for arrow
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(68,222),new GesturePoint(70,220),new GesturePoint(73,218),new GesturePoint(75,217),new GesturePoint(77,215),new GesturePoint(80,213),new GesturePoint(82,212),new GesturePoint(84,210),new GesturePoint(87,209),new GesturePoint(89,208),new GesturePoint(92,206),new GesturePoint(95,204),new GesturePoint(101,201),new GesturePoint(106,198),new GesturePoint(112,194),new GesturePoint(118,191),new GesturePoint(124,187),new GesturePoint(127,186),new GesturePoint(132,183),new GesturePoint(138,181),new GesturePoint(141,180),new GesturePoint(146,178),new GesturePoint(154,173),new GesturePoint(159,171),new GesturePoint(161,170),new GesturePoint(166,167),new GesturePoint(168,167),new GesturePoint(171,166),new GesturePoint(174,164),new GesturePoint(177,162),new GesturePoint(180,160),new GesturePoint(182,158),new GesturePoint(183,156),new GesturePoint(181,154),new GesturePoint(178,153),new GesturePoint(171,153),new GesturePoint(164,153),new GesturePoint(160,153),new GesturePoint(150,154),new GesturePoint(147,155),new GesturePoint(141,157),new GesturePoint(137,158),new GesturePoint(135,158),new GesturePoint(137,158),new GesturePoint(140,157),new GesturePoint(143,156),new GesturePoint(151,154),new GesturePoint(160,152),new GesturePoint(170,149),new GesturePoint(179,147),new GesturePoint(185,145),new GesturePoint(192,144),new GesturePoint(196,144),new GesturePoint(198,144),new GesturePoint(200,144),new GesturePoint(201,147),new GesturePoint(199,149),new GesturePoint(194,157),new GesturePoint(191,160),new GesturePoint(186,167),new GesturePoint(180,176),new GesturePoint(177,179),new GesturePoint(171,187),new GesturePoint(169,189),new GesturePoint(165,194),new GesturePoint(164,196));
			GestureTemplate arrow = new GestureTemplate("arrow", temp_points);
			add_template(arrow);

                        //adding points for left bracket
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(140,124),new GesturePoint(138,123),new GesturePoint(135,122),new GesturePoint(133,123),new GesturePoint(130,123),new GesturePoint(128,124),new GesturePoint(125,125),new GesturePoint(122,124),new GesturePoint(120,124),new GesturePoint(118,124),new GesturePoint(116,125),new GesturePoint(113,125),new GesturePoint(111,125),new GesturePoint(108,124),new GesturePoint(106,125),new GesturePoint(104,125),new GesturePoint(102,124),new GesturePoint(100,123),new GesturePoint(98,123),new GesturePoint(95,124),new GesturePoint(93,123),new GesturePoint(90,124),new GesturePoint(88,124),new GesturePoint(85,125),new GesturePoint(83,126),new GesturePoint(81,127),new GesturePoint(81,129),new GesturePoint(82,131),new GesturePoint(82,134),new GesturePoint(83,138),new GesturePoint(84,141),new GesturePoint(84,144),new GesturePoint(85,148),new GesturePoint(85,151),new GesturePoint(86,156),new GesturePoint(86,160),new GesturePoint(86,164),new GesturePoint(86,168),new GesturePoint(87,171),new GesturePoint(87,175),new GesturePoint(87,179),new GesturePoint(87,182),new GesturePoint(87,186),new GesturePoint(88,188),new GesturePoint(88,195),new GesturePoint(88,198),new GesturePoint(88,201),new GesturePoint(88,207),new GesturePoint(89,211),new GesturePoint(89,213),new GesturePoint(89,217),new GesturePoint(89,222),new GesturePoint(88,225),new GesturePoint(88,229),new GesturePoint(88,231),new GesturePoint(88,233),new GesturePoint(88,235),new GesturePoint(89,237),new GesturePoint(89,240),new GesturePoint(89,242),new GesturePoint(91,241),new GesturePoint(94,241),new GesturePoint(96,240),new GesturePoint(98,239),new GesturePoint(105,240),new GesturePoint(109,240),new GesturePoint(113,239),new GesturePoint(116,240),new GesturePoint(121,239),new GesturePoint(130,240),new GesturePoint(136,237),new GesturePoint(139,237),new GesturePoint(144,238),new GesturePoint(151,237),new GesturePoint(157,236),new GesturePoint(159,237));
			GestureTemplate leftBracket = new GestureTemplate("left square bracket", temp_points);
			add_template(leftBracket);

                         //adding points for right bracket
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(112,138),new GesturePoint(112,136),new GesturePoint(115,136),new GesturePoint(118,137),new GesturePoint(120,136),new GesturePoint(123,136),new GesturePoint(125,136),new GesturePoint(128,136),new GesturePoint(131,136),new GesturePoint(134,135),new GesturePoint(137,135),new GesturePoint(140,134),new GesturePoint(143,133),new GesturePoint(145,132),new GesturePoint(147,132),new GesturePoint(149,132),new GesturePoint(152,132),new GesturePoint(153,134),new GesturePoint(154,137),new GesturePoint(155,141),new GesturePoint(156,144),new GesturePoint(157,152),new GesturePoint(158,161),new GesturePoint(160,170),new GesturePoint(162,182),new GesturePoint(164,192),new GesturePoint(166,200),new GesturePoint(167,209),new GesturePoint(168,214),new GesturePoint(168,216),new GesturePoint(169,221),new GesturePoint(169,223),new GesturePoint(169,228),new GesturePoint(169,231),new GesturePoint(166,233),new GesturePoint(164,234),new GesturePoint(161,235),new GesturePoint(155,236),new GesturePoint(147,235),new GesturePoint(140,233),new GesturePoint(131,233),new GesturePoint(124,233),new GesturePoint(117,235),new GesturePoint(114,238),new GesturePoint(112,238));
			GestureTemplate rightBracket = new GestureTemplate("right square bracket", temp_points);
			add_template(rightBracket);

                        //adding points for v
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(89,164),new GesturePoint(90,162),new GesturePoint(92,162),new GesturePoint(94,164),new GesturePoint(95,166),new GesturePoint(96,169),new GesturePoint(97,171),new GesturePoint(99,175),new GesturePoint(101,178),new GesturePoint(103,182),new GesturePoint(106,189),new GesturePoint(108,194),new GesturePoint(111,199),new GesturePoint(114,204),new GesturePoint(117,209),new GesturePoint(119,214),new GesturePoint(122,218),new GesturePoint(124,222),new GesturePoint(126,225),new GesturePoint(128,228),new GesturePoint(130,229),new GesturePoint(133,233),new GesturePoint(134,236),new GesturePoint(136,239),new GesturePoint(138,240),new GesturePoint(139,242),new GesturePoint(140,244),new GesturePoint(142,242),new GesturePoint(142,240),new GesturePoint(142,237),new GesturePoint(143,235),new GesturePoint(143,233),new GesturePoint(145,229),new GesturePoint(146,226),new GesturePoint(148,217),new GesturePoint(149,208),new GesturePoint(149,205),new GesturePoint(151,196),new GesturePoint(151,193),new GesturePoint(153,182),new GesturePoint(155,172),new GesturePoint(157,165),new GesturePoint(159,160),new GesturePoint(162,155),new GesturePoint(164,150),new GesturePoint(165,148),new GesturePoint(166,146));
			GestureTemplate vtemp = new GestureTemplate("v", temp_points);
			add_template(vtemp);


                        //adding points for delete
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(123,129),new GesturePoint(123,131),new GesturePoint(124,133),new GesturePoint(125,136),new GesturePoint(127,140),new GesturePoint(129,142),new GesturePoint(133,148),new GesturePoint(137,154),new GesturePoint(143,158),new GesturePoint(145,161),new GesturePoint(148,164),new GesturePoint(153,170),new GesturePoint(158,176),new GesturePoint(160,178),new GesturePoint(164,183),new GesturePoint(168,188),new GesturePoint(171,191),new GesturePoint(175,196),new GesturePoint(178,200),new GesturePoint(180,202),new GesturePoint(181,205),new GesturePoint(184,208),new GesturePoint(186,210),new GesturePoint(187,213),new GesturePoint(188,215),new GesturePoint(186,212),new GesturePoint(183,211),new GesturePoint(177,208),new GesturePoint(169,206),new GesturePoint(162,205),new GesturePoint(154,207),new GesturePoint(145,209),new GesturePoint(137,210),new GesturePoint(129,214),new GesturePoint(122,217),new GesturePoint(118,218),new GesturePoint(111,221),new GesturePoint(109,222),new GesturePoint(110,219),new GesturePoint(112,217),new GesturePoint(118,209),new GesturePoint(120,207),new GesturePoint(128,196),new GesturePoint(135,187),new GesturePoint(138,183),new GesturePoint(148,167),new GesturePoint(157,153),new GesturePoint(163,145),new GesturePoint(165,142),new GesturePoint(172,133),new GesturePoint(177,127),new GesturePoint(179,127),new GesturePoint(180,125));
			GestureTemplate delete = new GestureTemplate("delete", temp_points);
			add_template(delete);

                        //adding points for left curly braces
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(150,116),new GesturePoint(147,117),new GesturePoint(145,116),new GesturePoint(142,116),new GesturePoint(139,117),new GesturePoint(136,117),new GesturePoint(133,118),new GesturePoint(129,121),new GesturePoint(126,122),new GesturePoint(123,123),new GesturePoint(120,125),new GesturePoint(118,127),new GesturePoint(115,128),new GesturePoint(113,129),new GesturePoint(112,131),new GesturePoint(113,134),new GesturePoint(115,134),new GesturePoint(117,135),new GesturePoint(120,135),new GesturePoint(123,137),new GesturePoint(126,138),new GesturePoint(129,140),new GesturePoint(135,143),new GesturePoint(137,144),new GesturePoint(139,147),new GesturePoint(141,149),new GesturePoint(140,152),new GesturePoint(139,155),new GesturePoint(134,159),new GesturePoint(131,161),new GesturePoint(124,166),new GesturePoint(121,166),new GesturePoint(117,166),new GesturePoint(114,167),new GesturePoint(112,166),new GesturePoint(114,164),new GesturePoint(116,163),new GesturePoint(118,163),new GesturePoint(120,162),new GesturePoint(122,163),new GesturePoint(125,164),new GesturePoint(127,165),new GesturePoint(129,166),new GesturePoint(130,168),new GesturePoint(129,171),new GesturePoint(127,175),new GesturePoint(125,179),new GesturePoint(123,184),new GesturePoint(121,190),new GesturePoint(120,194),new GesturePoint(119,199),new GesturePoint(120,202),new GesturePoint(123,207),new GesturePoint(127,211),new GesturePoint(133,215),new GesturePoint(142,219),new GesturePoint(148,220),new GesturePoint(151,221));
			GestureTemplate leftCurly = new GestureTemplate("left curly brace", temp_points);
			add_template(leftCurly);

                        //adding points for right curly braces
            		temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(117,132),new GesturePoint(115,132),new GesturePoint(115,129),new GesturePoint(117,129),new GesturePoint(119,128),new GesturePoint(122,127),new GesturePoint(125,127),new GesturePoint(127,127),new GesturePoint(130,127),new GesturePoint(133,129),new GesturePoint(136,129),new GesturePoint(138,130),new GesturePoint(140,131),new GesturePoint(143,134),new GesturePoint(144,136),new GesturePoint(145,139),new GesturePoint(145,142),new GesturePoint(145,145),new GesturePoint(145,147),new GesturePoint(145,149),new GesturePoint(144,152),new GesturePoint(142,157),new GesturePoint(141,160),new GesturePoint(139,163),new GesturePoint(137,166),new GesturePoint(135,167),new GesturePoint(133,169),new GesturePoint(131,172),new GesturePoint(128,173),new GesturePoint(126,176),new GesturePoint(125,178),new GesturePoint(125,180),new GesturePoint(125,182),new GesturePoint(126,184),new GesturePoint(128,187),new GesturePoint(130,187),new GesturePoint(132,188),new GesturePoint(135,189),new GesturePoint(140,189),new GesturePoint(145,189),new GesturePoint(150,187),new GesturePoint(155,186),new GesturePoint(157,185),new GesturePoint(159,184),new GesturePoint(156,185),new GesturePoint(154,185),new GesturePoint(149,185),new GesturePoint(145,187),new GesturePoint(141,188),new GesturePoint(136,191),new GesturePoint(134,191),new GesturePoint(131,192),new GesturePoint(129,193),new GesturePoint(129,195),new GesturePoint(129,197),new GesturePoint(131,200),new GesturePoint(133,202),new GesturePoint(136,206),new GesturePoint(139,211),new GesturePoint(142,215),new GesturePoint(145,220),new GesturePoint(147,225),new GesturePoint(148,231),new GesturePoint(147,239),new GesturePoint(144,244),new GesturePoint(139,248),new GesturePoint(134,250),new GesturePoint(126,253),new GesturePoint(119,253),new GesturePoint(115,253));
			GestureTemplate rightCurly = new GestureTemplate("right curly brace", temp_points);
			add_template(rightCurly);

                        //adding points for start
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(75,250),new GesturePoint(75,247),new GesturePoint(77,244),new GesturePoint(78,242),new GesturePoint(79,239),new GesturePoint(80,237),new GesturePoint(82,234),new GesturePoint(82,232),new GesturePoint(84,229),new GesturePoint(85,225),new GesturePoint(87,222),new GesturePoint(88,219),new GesturePoint(89,216),new GesturePoint(91,212),new GesturePoint(92,208),new GesturePoint(94,204),new GesturePoint(95,201),new GesturePoint(96,196),new GesturePoint(97,194),new GesturePoint(98,191),new GesturePoint(100,185),new GesturePoint(102,178),new GesturePoint(104,173),new GesturePoint(104,171),new GesturePoint(105,164),new GesturePoint(106,158),new GesturePoint(107,156),new GesturePoint(107,152),new GesturePoint(108,145),new GesturePoint(109,141),new GesturePoint(110,139),new GesturePoint(112,133),new GesturePoint(113,131),new GesturePoint(116,127),new GesturePoint(117,125),new GesturePoint(119,122),new GesturePoint(121,121),new GesturePoint(123,120),new GesturePoint(125,122),new GesturePoint(125,125),new GesturePoint(127,130),new GesturePoint(128,133),new GesturePoint(131,143),new GesturePoint(136,153),new GesturePoint(140,163),new GesturePoint(144,172),new GesturePoint(145,175),new GesturePoint(151,189),new GesturePoint(156,201),new GesturePoint(161,213),new GesturePoint(166,225),new GesturePoint(169,233),new GesturePoint(171,236),new GesturePoint(174,243),new GesturePoint(177,247),new GesturePoint(178,249),new GesturePoint(179,251),new GesturePoint(180,253),new GesturePoint(180,255),new GesturePoint(179,257),new GesturePoint(177,257),new GesturePoint(174,255),new GesturePoint(169,250),new GesturePoint(164,247),new GesturePoint(160,245),new GesturePoint(149,238),new GesturePoint(138,230),new GesturePoint(127,221),new GesturePoint(124,220),new GesturePoint(112,212),new GesturePoint(110,210),new GesturePoint(96,201),new GesturePoint(84,195),new GesturePoint(74,190),new GesturePoint(64,182),new GesturePoint(55,175),new GesturePoint(51,172),new GesturePoint(49,170),new GesturePoint(51,169),new GesturePoint(56,169),new GesturePoint(66,169),new GesturePoint(78,168),new GesturePoint(92,166),new GesturePoint(107,164),new GesturePoint(123,161),new GesturePoint(140,162),new GesturePoint(156,162),new GesturePoint(171,160),new GesturePoint(173,160),new GesturePoint(186,160),new GesturePoint(195,160),new GesturePoint(198,161),new GesturePoint(203,163),new GesturePoint(208,163),new GesturePoint(206,164),new GesturePoint(200,167),new GesturePoint(187,172),new GesturePoint(174,179),new GesturePoint(172,181),new GesturePoint(153,192),new GesturePoint(137,201),new GesturePoint(123,211),new GesturePoint(112,220),new GesturePoint(99,229),new GesturePoint(90,237),new GesturePoint(80,244),new GesturePoint(73,250),new GesturePoint(69,254),new GesturePoint(69,252));
			GestureTemplate star = new GestureTemplate("Star", temp_points);
			add_template(star);


                        ////adding points for pigtail
			temp_points = new ArrayList<>();
			add_templatePoints(new GesturePoint(81,219),new GesturePoint(84,218),new GesturePoint(86,220),new GesturePoint(88,220),new GesturePoint(90,220),new GesturePoint(92,219),new GesturePoint(95,220),new GesturePoint(97,219),new GesturePoint(99,220),new GesturePoint(102,218),new GesturePoint(105,217),new GesturePoint(107,216),new GesturePoint(110,216),new GesturePoint(113,214),new GesturePoint(116,212),new GesturePoint(118,210),new GesturePoint(121,208),new GesturePoint(124,205),new GesturePoint(126,202),new GesturePoint(129,199),new GesturePoint(132,196),new GesturePoint(136,191),new GesturePoint(139,187),new GesturePoint(142,182),new GesturePoint(144,179),new GesturePoint(146,174),new GesturePoint(148,170),new GesturePoint(149,168),new GesturePoint(151,162),new GesturePoint(152,160),new GesturePoint(152,157),new GesturePoint(152,155),new GesturePoint(152,151),new GesturePoint(152,149),new GesturePoint(152,146),new GesturePoint(149,142),new GesturePoint(148,139),new GesturePoint(145,137),new GesturePoint(141,135),new GesturePoint(139,135),new GesturePoint(134,136),new GesturePoint(130,140),new GesturePoint(128,142),new GesturePoint(126,145),new GesturePoint(122,150),new GesturePoint(119,158),new GesturePoint(117,163),new GesturePoint(115,170),new GesturePoint(114,175),new GesturePoint(117,184),new GesturePoint(120,190),new GesturePoint(125,199),new GesturePoint(129,203),new GesturePoint(133,208),new GesturePoint(138,213),new GesturePoint(145,215),new GesturePoint(155,218),new GesturePoint(164,219),new GesturePoint(166,219),new GesturePoint(177,219),new GesturePoint(182,218),new GesturePoint(192,216),new GesturePoint(196,213),new GesturePoint(199,212),new GesturePoint(201,211));
			GestureTemplate pigtail = new GestureTemplate("pigtail", temp_points);
			add_template(pigtail);
	    }

	}
