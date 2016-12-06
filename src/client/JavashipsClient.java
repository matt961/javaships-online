package client;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import static protocol.JavashipsProtocol.*;

public class JavashipsClient {

	// create variables for grids and buttons
	private GridValue[][] OppGrid;
	private JButton[][] OppButtons;
	private GridValue[][] PlayerGrid;
	private JButton[][] PlayerButtons;
	private JButton[] shipButtons = new JButton[5];
	private JTextArea chatArea;

	// Use these for communicating with your opponent.
	private Socket server;
	private PrintWriter commandWriter;
	private BufferedReader commandReader;
	private String host = "127.0.0.1";
	private int port = 9876;
	private Thread networkListener;
	private volatile boolean hasConnection;

	// create state variables for game play
	private boolean isFirstAttacker;
	private boolean opponentReady;
	private boolean sentReady;
	private GameState gameState = GameState.NOGAME;
	private GridValue placeState = GridValue.EMPTY;
	private Orientation placeOrient = Orientation.VERTICAL;

	private int
			hpBATTLESHIP,
			hpCRUISER,
			hpDESTROYER,
			hpSUBMARINE,
			hpCARRIER;

	private JFrame frame;
	private JFrame messagePane;
	private JTextField ChatTextField;
	private JLabel GameStateText;
	private JMenuItem mntmStartGame;
	private JMenuItem mntmQuitGame;

	/**
	 * Create the application.
	 */
	private JavashipsClient() {
		initialize();
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		JavashipsClient window = new JavashipsClient();
		window.frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		hasConnection = false;
		isFirstAttacker = false;
		opponentReady = false;

		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 685, 636);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		Label PlayersLabel = new Label("Players");
		PlayersLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		PlayersLabel.setBounds(12, 2, 50, 11);
		frame.getContentPane().add(PlayersLabel);

		JPanel PlayersPanel = new JPanel();
		PlayersPanel.setName("");
		PlayersPanel.setBorder(new LineBorder(SystemColor.inactiveCaption, 1, true));
		PlayersPanel.setBounds(2, 11, 280, 569);
		frame.getContentPane().add(PlayersPanel);
		PlayersPanel.setLayout(null);

		Label PlayerLabel = new Label("Player");
		PlayerLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		PlayerLabel.setBounds(15, 284, 44, 16);
		PlayersPanel.add(PlayerLabel);

		JPanel PlayerPanel = new JPanel();
		PlayerPanel.setLayout(null);
		PlayerPanel.setName("");
		PlayerPanel.setBorder(new LineBorder(SystemColor.inactiveCaption, 1, true));
		PlayerPanel.setBounds(4, 294, 272, 270);
		PlayersPanel.add(PlayerPanel);

		JPanel PlayerGridPanel = new JPanel();
		PlayerGridPanel.setForeground(Color.WHITE);
		PlayerGridPanel.setBackground(SystemColor.window);
		PlayerGridPanel.setBounds(10, 10, 250, 250);
		PlayerPanel.add(PlayerGridPanel);
		PlayerGridPanel.setLayout(new GridLayout(10, 10, 0, 0));


		// ====================================================================================================================
		// create buttons and grid for player
		// ====================================================================================================================

		PlayerGrid = new GridValue[10][10];
		PlayerButtons = new JButton[10][10];
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				PlayerGrid[i][j] = GridValue.EMPTY;
				JButton button = new JButton(i + "" + j);
				PlayerButtons[i][j] = button;
				PlayerButtons[i][j].setEnabled(false);
				PlayerButtons[i][j].setBorder(new LineBorder(new Color(0, 0, 0)));
				//PlayerButtons[i][j].setBorder(new CompoundBorder(new LineBorder(new Color(0, 0, 0)), new LineBorder(new Color(255, 255, 255), 2)));
				PlayerButtons[i][j].setBackground(SystemColor.window);

				// create player mouse listener for different states
				PlayerButtons[i][j].addMouseListener(new java.awt.event.MouseAdapter() {
					@Override
					public void mouseEntered(java.awt.event.MouseEvent evt) {
						switch (gameState) {
							case NOGAME:
								break;
							case SETUP:
								// which
								switch (placeState) {
									case CARRIER:
										shipOver(button, 5);
										break;
									case BATTLESHIP:
										shipOver(button, 4);
										break;
									case CRUISER:
										shipOver(button, 3);
										break;
									case DESTROYER:
										shipOver(button, 2);
										break;
									case SUBMARINE:
										shipOver(button, 3);
										break;
									default:
										break;
								}
								break;
							case PLAY:
								break;
							case GAMEOVER:
								break;
						}
					}

					@Override
					public void mouseExited(java.awt.event.MouseEvent evt) {
						switch (gameState) {
							case NOGAME:
								break;
							case SETUP:
								draw();
								break;
							case PLAY:
								break;
							case GAMEOVER:
								break;
						}
					}

					@Override
					public void mousePressed(java.awt.event.MouseEvent evt) {
						switch (gameState) {
							case NOGAME:
								break;
							case SETUP:
								// which
								switch (placeState) {
									case CARRIER:
										shipSet(button, 5);
										break;
									case BATTLESHIP:
										shipSet(button, 4);
										break;
									case CRUISER:
										shipSet(button, 3);
										break;
									case DESTROYER:
										shipSet(button, 2);
										break;
									case SUBMARINE:
										shipSet(button, 3);
										break;
									default:
										break;
								}
								break;
							case PLAY:
								break;
							case GAMEOVER:
								break;
							default:
								break;
						}
					}
				});
				PlayerGridPanel.add(PlayerButtons[i][j]);
			}
		}


		Label OpponentLabel = new Label("Opponent");
		OpponentLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		OpponentLabel.setBounds(12, 6, 62, 15);
		PlayersPanel.add(OpponentLabel);

		JPanel OpponentPanel = new JPanel();
		OpponentPanel.setBounds(4, 14, 272, 270);
		PlayersPanel.add(OpponentPanel);
		OpponentPanel.setLayout(null);
		OpponentPanel.setName("");
		OpponentPanel.setBorder(new LineBorder(SystemColor.inactiveCaption, 1, true));

		JPanel OpponentGridPanel = new JPanel();
		OpponentGridPanel.setForeground(Color.WHITE);
		OpponentGridPanel.setBackground(SystemColor.window);
		OpponentGridPanel.setBounds(10, 10, 250, 250);
		OpponentPanel.add(OpponentGridPanel);
		OpponentGridPanel.setLayout(new GridLayout(10, 10, 0, 0));

		Label GameStateLabel = new Label("Game State");
		GameStateLabel.setBounds(293, 3, 70, 15);
		frame.getContentPane().add(GameStateLabel);
		GameStateLabel.setFont(new Font("Dialog", Font.BOLD, 12));

		JPanel GameStatePanel = new JPanel();
		GameStatePanel.setLayout(null);
		GameStatePanel.setName("");
		GameStatePanel.setBorder(new LineBorder(SystemColor.inactiveCaption, 1, true));
		GameStatePanel.setBounds(285, 11, 389, 63);
		frame.getContentPane().add(GameStatePanel);

		GameStateText = new JLabel("Start a NEW GAME");
		GameStateText.setForeground(Color.GRAY);
		GameStateText.setFont(new Font("Tahoma", Font.BOLD, 11));
		GameStateText.setHorizontalTextPosition(SwingConstants.CENTER);
		GameStateText.setHorizontalAlignment(SwingConstants.CENTER);
		GameStateText.setAlignmentX(Component.CENTER_ALIGNMENT);
		GameStateText.setBounds(10, 11, 369, 41);
		GameStatePanel.add(GameStateText);

		Label GamePieceLabel = new Label("Ships");
		GamePieceLabel.setBounds(293, 75, 37, 15);
		frame.getContentPane().add(GamePieceLabel);
		GamePieceLabel.setFont(new Font("Dialog", Font.BOLD, 12));

		JPanel GamePiecePanel = new JPanel();
		GamePiecePanel.setLayout(null);
		GamePiecePanel.setName("");
		GamePiecePanel.setBorder(new LineBorder(SystemColor.inactiveCaption, 1, true));
		GamePiecePanel.setBounds(285, 82, 389, 213);
		frame.getContentPane().add(GamePiecePanel);


		// ====================================================================================================================
		// create buttons for placing ships
		// ====================================================================================================================


		JButton btnCarrier = new JButton("Place Carrier");
		btnCarrier.setEnabled(false);
		btnCarrier.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (placeState != GridValue.CARRIER) {
					placeOrient = Orientation.HORIZONTAL;
					GameStateText.setText("Place CARRIER HORIZONTAL (Click again for VERTICAL)");
				}
				else {
					if (placeOrient == Orientation.HORIZONTAL) {
						placeOrient = Orientation.VERTICAL;
						GameStateText.setText("Place CARRIER VERTICAL (Click again for VERTICAL)");
					}
					else {
						placeOrient = Orientation.HORIZONTAL;
						GameStateText.setText("Place CARRIER HORIZONTAL (Click again for HORIZONTAL)");
					}
				}
				placeState = GridValue.CARRIER;
			}
		});
		btnCarrier.setBounds(32, 25, 145, 23);
		GamePiecePanel.add(btnCarrier);
		shipButtons[0] = btnCarrier;

		JButton btnBattleship = new JButton("Place Battleship");
		btnBattleship.setEnabled(false);
		btnBattleship.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (placeState != GridValue.BATTLESHIP) {
					placeOrient = Orientation.HORIZONTAL;
					GameStateText.setText("Place BATTLESHIP HORIZONTAL (Click again for VERTICAL)");
				}
				else {
					if (placeOrient == Orientation.HORIZONTAL) {
						placeOrient = Orientation.VERTICAL;
						GameStateText.setText("Place BATTLESHIP VERTICAL (Click again for VERTICAL)");
					}
					else {
						placeOrient = Orientation.HORIZONTAL;
						GameStateText.setText("Place BATTLESHIP HORIZONTAL (Click again for HORIZONTAL)");
					}
				}
				placeState = GridValue.BATTLESHIP;
			}
		});
		btnBattleship.setBounds(32, 59, 145, 23);
		GamePiecePanel.add(btnBattleship);
		shipButtons[1] = btnBattleship;

		JButton btnCruiser = new JButton("Place Cruiser");
		btnCruiser.setEnabled(false);
		btnCruiser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (placeState != GridValue.CRUISER) {
					placeOrient = Orientation.HORIZONTAL;
					GameStateText.setText("Place CRUISER HORIZONTAL (Click again for VERTICAL)");
				}
				else {
					if (placeOrient == Orientation.HORIZONTAL) {
						placeOrient = Orientation.VERTICAL;
						GameStateText.setText("Place CRUISER VERTICAL (Click again for VERTICAL)");
					}
					else {
						placeOrient = Orientation.HORIZONTAL;
						GameStateText.setText("Place CRUISER HORIZONTAL (Click again for HORIZONTAL)");
					}
				}
				placeState = GridValue.CRUISER;
			}
		});
		btnCruiser.setBounds(32, 93, 145, 23);
		GamePiecePanel.add(btnCruiser);
		shipButtons[2] = btnCruiser;

		JButton btnSubmarine = new JButton("Place Submarine");
		btnSubmarine.setEnabled(false);
		btnSubmarine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (placeState != GridValue.SUBMARINE) {
					placeOrient = Orientation.HORIZONTAL;
					GameStateText.setText("Place SUBMARINE HORIZONTAL (Click again for VERTICAL)");
				}
				else {
					if (placeOrient == Orientation.HORIZONTAL) {
						placeOrient = Orientation.VERTICAL;
						GameStateText.setText("Place SUBMARINE VERTICAL (Click again for VERTICAL)");
					}
					else {
						placeOrient = Orientation.HORIZONTAL;
						GameStateText.setText("Place SUBMARINE HORIZONTAL (Click again for HORIZONTAL)");
					}
				}
				placeState = GridValue.SUBMARINE;
			}
		});
		btnSubmarine.setBounds(32, 127, 145, 23);
		GamePiecePanel.add(btnSubmarine);
		shipButtons[3] = btnSubmarine;

		JButton btnDestroyer = new JButton("Place Destroyer");
		btnDestroyer.setEnabled(false);
		btnDestroyer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (placeState != GridValue.DESTROYER) {
					placeOrient = Orientation.HORIZONTAL;
					GameStateText.setText("Place DESTROYER HORIZONTAL (Click again for VERTICAL)");
				}
				else {
					if (placeOrient == Orientation.HORIZONTAL) {
						placeOrient = Orientation.VERTICAL;
						GameStateText.setText("Place DESTROYER VERTICAL (Click again for VERTICAL)");
					}
					else {
						placeOrient = Orientation.HORIZONTAL;
						GameStateText.setText("Place DESTROYER HORIZONTAL (Click again for HORIZONTAL)");
					}
				}
				placeState = GridValue.DESTROYER;
			}
		});
		btnDestroyer.setBounds(32, 161, 145, 23);
		GamePiecePanel.add(btnDestroyer);
		shipButtons[4] = btnDestroyer;

		JLabel lblSize = new JLabel("Size: 5");
		lblSize.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSize.setBounds(203, 25, 41, 23);
		GamePiecePanel.add(lblSize);

		JLabel lblSize_1 = new JLabel("Size: 4");
		lblSize_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSize_1.setBounds(203, 59, 41, 23);
		GamePiecePanel.add(lblSize_1);

		JLabel lblSize_2 = new JLabel("Size: 3");
		lblSize_2.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSize_2.setBounds(203, 93, 41, 23);
		GamePiecePanel.add(lblSize_2);

		JLabel lblSize_3 = new JLabel("Size: 3");
		lblSize_3.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSize_3.setBounds(203, 127, 41, 23);
		GamePiecePanel.add(lblSize_3);

		JLabel lblSize_4 = new JLabel("Size: 2");
		lblSize_4.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSize_4.setBounds(203, 161, 41, 23);
		GamePiecePanel.add(lblSize_4);


		// ====================================================================================================================
		// CHAT BOX
		// ====================================================================================================================

		Label label = new Label("Chat");
		label.setFont(new Font("Dialog", Font.BOLD, 12));
		label.setBounds(293, 296, 37, 15);
		frame.getContentPane().add(label);

		JPanel ChatPanel = new JPanel();
		ChatPanel.setLayout(null);
		ChatPanel.setName("");
		ChatPanel.setBorder(new LineBorder(SystemColor.inactiveCaption, 1, true));
		ChatPanel.setBounds(285, 303, 389, 277);
		frame.getContentPane().add(ChatPanel);

		chatArea = new JTextArea();
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);

		JScrollPane sp = new JScrollPane(chatArea);
		sp.setBounds(10, 11, 369, 224);
		ChatPanel.add(sp);

		ChatTextField = new JTextField();
		ChatTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendChat();
			}
		});
		ChatTextField.setBounds(10, 241, 301, 27);
		ChatPanel.add(ChatTextField);
		ChatTextField.setColumns(10);

		Button SendButton = new Button("Send");
		SendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sendChat();
			}
		});
		SendButton.setBounds(317, 241, 62, 27);
		ChatPanel.add(SendButton);
		ChatPanel.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{sp, chatArea, ChatTextField, SendButton}));

		// ====================================================================================================================
		// create buttons and grid for opponent
		// ====================================================================================================================

		OppGrid = new GridValue[10][10];
		OppButtons = new JButton[10][10];
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				OppGrid[i][j] = GridValue.EMPTY;
				JButton button = new JButton("");
				OppButtons[i][j] = button;
				OppButtons[i][j].setEnabled(false);
				OppButtons[i][j].setBorder(new LineBorder(new Color(0, 0, 0)));
				//OppButtons[i][j].setBorder(new CompoundBorder(new LineBorder(new Color(0, 0, 0)), new LineBorder(new Color(255, 255, 255), 2)));
				OppButtons[i][j].setBackground(SystemColor.window);
				// create player mouse listener for different states

				OppButtons[i][j].addMouseListener(new java.awt.event.MouseAdapter() {
					@Override
					public void mouseEntered(java.awt.event.MouseEvent evt) {
						switch (gameState) {
							case NOGAME:
								// DO NOTHING. PLAYER DOESN'T NEED TO DO
								// ANYTHING WITH OPPONENTS GRID IN NOGAME.
								break;
							case SETUP:
								// DO NOTHING. PLAYER DOESN'T NEED TO DO
								// ANYTHING WITH OPPONENTS GRID IN SETUP.
								break;
							case WAIT:
								break;
							case PLAY:
								if (button.getBackground().equals(GridColor.MISS.c) ||
										button.getBackground().equals(GridColor.HIT.c)) {
									; //do nothing
								}
								else {
									button.setBackground(GridColor.SELECTED.c);
								}

								// SEND MOUSE OVER VALUE TO OPPONENT FOR FEAR FACTOR.
								JButton seeked = (JButton) evt.getSource();

								for (int i = 0; i < 10; i++) {
									for (int j = 0; j < 10; j++) {

										if (OppButtons[i][j] == seeked) {
											sendSeek(commandWriter, i, j);
											break;
										}

									}
								}
								break;
							case GAMEOVER:
								break;
						}
					}

					@Override
					public void mouseExited(java.awt.event.MouseEvent evt) {
						switch (gameState) {
							case NOGAME:
								// DO NOTHING. PLAYER DOESN'T NEED TO DO
								// ANYTHING WITH OPPONENTS GRID IN SETUP.
								break;
							case SETUP:
								// DO NOTHING. PLAYER DOESN'T NEED TO DO
								// ANYTHING WITH OPPONENTS GRID IN SETUP.
								break;
							case WAIT:
								break;
							case PLAY:
								if (button.getBackground().equals(GridColor.MISS.c) ||
										button.getBackground().equals(GridColor.HIT.c)) {
									; //do nothing
								}
								else {
									button.setBackground(GridColor.EMPTY.c);
								}

								sendRedraw(commandWriter);
								break;
							case GAMEOVER:
								break;
						}
					}

					@Override
					public void mouseClicked(java.awt.event.MouseEvent evt) {
						JButton clicked = (JButton) evt.getSource();

						switch (gameState) {
							case NOGAME:
								// DO NOTHING. PLAYER DOESN'T NEED TO DO
								// ANYTHING WITH OPPONENTS GRID IN SETUP.
								break;
							case SETUP:
								// DO NOTHING. PLAYER DOESN'T NEED TO DO
								// ANYTHING WITH OPPONENTS GRID IN SETUP.
								break;
							case WAIT:
								break;
							case PLAY:
								for (int x = 0; x < 10; x++) {
									for (int y = 0; y < 10; y++) {

										if (OppButtons[x][y].equals(clicked)) {

											if (OppGrid[x][y] == GridValue.EMPTY) {
												sendAttack(commandWriter, x, y);
												gameState = GameState.WAIT;
												break;
											}
											else {
												break;
											}
										}
									}
								}

								sendRedraw(commandWriter);

								break;

							case GAMEOVER:
								break;
						}
					}
				});
				OpponentGridPanel.add(OppButtons[i][j]);
			}
		}

		JMenuBar MenuBar = new JMenuBar();
		frame.setJMenuBar(MenuBar);

		JMenu mnMenu = new JMenu("Menu");
		MenuBar.add(mnMenu);

		mntmStartGame = new JMenuItem("Start Game");
		mntmStartGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mntmStartGame.setEnabled(false);
				gameState = GameState.SETUP;
				startGame();
				GameStateText.setText("Choose ship below to place");
			}
		});
		mntmStartGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mnMenu.add(mntmStartGame);

		mntmQuitGame = new JMenuItem("Quit Game");
		mntmQuitGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quitGame();
			}
		});
		mntmQuitGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		mnMenu.add(mntmQuitGame);

		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		mnMenu.add(mntmAbout);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		mnMenu.add(mntmExit);
		frame.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{MenuBar, mnMenu, mntmStartGame, mntmQuitGame, PlayersLabel, mntmAbout, mntmExit, SendButton, chatArea, ChatTextField, sp, ChatPanel, label, GamePiecePanel, GamePieceLabel, GameStateText, GameStatePanel, GameStateLabel, OpponentLabel, PlayersPanel, frame.getContentPane(), OpponentGridPanel, OpponentPanel, PlayerLabel, PlayerPanel, PlayerGridPanel}));

		draw();
	}

	private void draw() {
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				// draw opponents grid
				switch (OppGrid[i][j]) {
					case EMPTY:
						OppButtons[i][j].setBackground(GridColor.EMPTY.c);
						break;
					case CARRIER:
						break;
					case BATTLESHIP:
						break;
					case CRUISER:
						break;
					case SUBMARINE:
						break;
					case DESTROYER:
						break;
					case HIT:
						OppButtons[i][j].setBackground(GridColor.HIT.c);
						break;
					case MISS:
						OppButtons[i][j].setBackground(GridColor.MISS.c);
						break;
					case SELECTED:
						OppButtons[i][j].setBackground(GridColor.SELECTED.c);
						break;
					case SEEKING:
						PlayerButtons[i][j].setBackground(GridColor.SEEKING.c);
						break;
				}
				// draw players grid
				switch (PlayerGrid[i][j]) {
					case EMPTY:
						PlayerButtons[i][j].setBackground(GridColor.EMPTY.c);
						PlayerButtons[i][j].setBorder(new LineBorder(new Color(0, 0, 0)));
						break;
					case HIT:
						PlayerButtons[i][j].setBackground(GridColor.HIT.c);
						break;
					case MISS:
						PlayerButtons[i][j].setBackground(GridColor.MISS.c);
						break;
					case SELECTED:
						PlayerButtons[i][j].setBackground(GridColor.SELECTED.c);
						break;
					default:
						PlayerButtons[i][j].setBackground(GridColor.SELECTED.c);
						PlayerButtons[i][j].setBorder(new CompoundBorder(new LineBorder(new Color(0, 0, 0)), new LineBorder(new Color(255, 255, 255), 2)));
						break;
				}
			}
		}
	}

	/**
	 * Sends the other player a message. Used in the chatArea listener.
	 */
	private void sendChat() {
		String message = ChatTextField.getText();
		if (!message.equals("")) {
			chatArea.setText(chatArea.getText() + "You: " + ChatTextField.getText() + "\n");
			ChatTextField.setText("");

			try {
				sendMessage(commandWriter, message);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void shipSet(JButton btn, int size) {
		int i, j, x, offset = 0;
		draw();
		for (i = 0; i < 10; i++) {
			for (j = 0; j < 10; j++) {
				if (btn == PlayerButtons[i][j]) {
					switch (placeOrient) {
						// Horizontal placement
						case HORIZONTAL:
							if (10 - j < size) {
								offset = j - (10 - size);
							}
							for (x = 0; x < size; x++) {
								if (PlayerGrid[i][j + x - offset] != GridValue.EMPTY) {
									GameStateText.setText("Cannot place ship there");
									return;
								}
							}
							for (x = 0; x < size; x++) {
								PlayerGrid[i][j + x - offset] = placeState;
							}
							break;
						// Vertical placement
						case VERTICAL:
							if (10 - i < size) {
								offset = i - (10 - size);
							}
							for (x = 0; x < size; x++) {
								if (PlayerGrid[i + x - offset][j] != GridValue.EMPTY) {
									GameStateText.setText("Cannot place ship there");
									return;
								}
							}
							for (x = 0; x < size; x++) {
								PlayerGrid[i + x - offset][j] = placeState;
							}
							break;
						default:
							break;
					}
					shipButtons[placeState.num].setEnabled(false);
					placeState = GridValue.EMPTY;
					checkStartGame();
					draw();
					return;
				}
			}
		}

	}

	private void shipOver(JButton btn, int size) {
		int i, j, x, offset = 0;
		draw();
		for (i = 0; i < 10; i++) {
			for (j = 0; j < 10; j++) {
				if (btn == PlayerButtons[i][j]) {
					switch (placeOrient) {
						// Horizontal placement
						case HORIZONTAL:
							if (10 - j < size) {
								offset = j - (10 - size);
							}
							for (x = 0; x < size; x++) {
								if (PlayerGrid[i][j + x - offset] != GridValue.EMPTY) {
									PlayerButtons[i][j + x - offset].setBackground(GridColor.HIT.c);
								}
								else {
									PlayerButtons[i][j + x - offset].setBackground(GridColor.SELECTED.c);
								}
							}
							break;
						// Vertical placement
						case VERTICAL:
							if (10 - i < size) {
								offset = i - (10 - size);
							}
							for (x = 0; x < size; x++) {
								if (PlayerGrid[i + x - offset][j] != GridValue.EMPTY) {
									PlayerButtons[i + x - offset][j].setBackground(GridColor.HIT.c);
								}
								else {
									PlayerButtons[i + x - offset][j].setBackground(GridColor.SELECTED.c);
								}
							}
							break;
						default:
							break;
					}
					return;
				}
			}
		}

	}

	private void checkStartGame() {
		int i, j, count = 0;
		for (i = 0; i < 10; i++) {
			for (j = 0; j < 10; j++) {
				if (PlayerGrid[i][j] != GridValue.EMPTY) {
					count++;
				}
			}
		}
		if (count == 17) {
			if (!sentReady) {
				sendReady(commandWriter);
				sentReady = !sentReady;
			}

			if (isFirstAttacker && opponentReady) {
				gameState = GameState.PLAY;
			}
			else {
				gameState = GameState.WAIT;
			}

//			gameState = GameState.PLAY;
			GameStateText.setText("You've placed all your ships!");
		}
	}

	/**
	 * If a ship's HP is 0, send your opponent a message.
	 *
	 * @param hitShip The type of ship that your opponent hit.
	 * @throws Exception
	 */
	private void checkSunkenShips(String hitShip) throws Exception {
		if (hitShip == null) {
			throw new Exception("Can't process a null GridValue.");
		}

		if (hitShip.equals(GridValue.BATTLESHIP.toString())) {
			if (hpBATTLESHIP <= 0) {
				sendMessage(commandWriter, "You sunk my BATTLESHIP!\n");
			}
		}
		else if (hitShip.equals(GridValue.CARRIER.toString())) {
			if (hpCARRIER <= 0) {
				sendMessage(commandWriter, "You sunk my CARRIER!\n");
			}
		}
		else if (hitShip.equals(GridValue.CRUISER.toString())) {
			if (hpCRUISER <= 0) {
				sendMessage(commandWriter, "You sunk my CRUISER!\n");
			}
		}
		else if (hitShip.equals(GridValue.DESTROYER.toString())) {
			if (hpDESTROYER <= 0) {
				sendMessage(commandWriter, "You sunk my DESTROYER! :(\n");
			}
		}
		else if (hitShip.equals(GridValue.SUBMARINE.toString())) {
			if (hpSUBMARINE <= 0) {
				sendMessage(commandWriter, "You sunk my SUBMARINE! :(\n");
			}
		}
		else {
			throw new Exception("Should never happen. None of the hitShip values were actually a ship.");
		}
	}

	/**
	 * If all ship hit points summed is 0, then the other player has killed all of your ships! :(
	 *
	 * @return true if all ships are killed.
	 */
	private boolean checkIfLost() {
		return ((hpSUBMARINE + hpDESTROYER + hpCRUISER + hpCARRIER + hpBATTLESHIP) <= 0);
	}

	// ====================================================================================================================
	// PLACE SHIP FUNCTION to change the state of the Player grid
	// ERROR if ship already exists at the location ship is to be place
	// ====================================================================================================================

	private void startGame() {
		sentReady = false;
		isFirstAttacker = false;

		networkListener = new JavashipsNetworkingThread();
		networkListener.start();

		for (int i = 0; i < 5; i++) {
			shipButtons[i].setEnabled(true);
		}
		mntmQuitGame.setEnabled(true);

		hpBATTLESHIP = 4;
		hpCARRIER = 5;
		hpCRUISER = 3;
		hpSUBMARINE = 3;
		hpDESTROYER = 2;
	}

	private void quitGame() {
		gameState = GameState.NOGAME;
		opponentReady = false;

		for (int i = 0; i < 5; i++) {
			shipButtons[i].setEnabled(false);
		}

		int i, j;
		for (i = 0; i < 10; i++) {
			for (j = 0; j < 10; j++) {
				OppGrid[i][j] = GridValue.EMPTY;
				PlayerGrid[i][j] = GridValue.EMPTY;
			}
		}

		mntmStartGame.setEnabled(true);
		mntmQuitGame.setEnabled(false);
		draw();


		sendQuit(commandWriter);
		closeConnection();
	}

	private void gameOver() {
		gameState = GameState.NOGAME;
		opponentReady = false;

		for (int i = 0; i < 5; i++) {
			shipButtons[i].setEnabled(false);
		}

		int i, j;
		for (i = 0; i < 10; i++) {
			for (j = 0; j < 10; j++) {
				OppGrid[i][j] = GridValue.EMPTY;
				PlayerGrid[i][j] = GridValue.EMPTY;
			}
		}

		mntmStartGame.setEnabled(true);
		mntmQuitGame.setEnabled(false);
		draw();

		closeConnection();
	}

	// ====================================================================================================================
	// PLACE SHIP FUNCTION to change the state of the Player grid
	// ERROR if ship already exists at the location ship is to be place
	// ====================================================================================================================

	private void closeConnection() {
		try {
			hasConnection = false;
			server.close();
			commandWriter.close();
			commandReader.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// grid values
	public enum GridValue {
		CARRIER(0),
		BATTLESHIP(1),
		CRUISER(2),
		SUBMARINE(3),
		DESTROYER(4),
		EMPTY(5),
		HIT(6),
		SELECTED(7),
		SEEKING(8),
		MISS(9);

		private final int num;

		GridValue(int num) {
			this.num = num;
		}
	}

	// ====================================================================================================================
	// HOVER SELECT SHIP FUNCTION to change the state of the Player grid
	// DISPLAY RED if ship already exists at the location ship is to be place
	// ====================================================================================================================

	// enum for GridColors
	public enum GridColor {
		EMPTY(new Color(0x1C6BA0)),
		HIT(Color.RED),
		MISS(Color.WHITE),
		SELECTED(SystemColor.LIGHT_GRAY),
		SEEKING(SystemColor.ORANGE);

		private final Color c;

		GridColor(Color c) {
			this.c = c;
		}
	}

	// Game states
	private enum GameState {
		NOGAME,
		SETUP,
		PLAY,
		GAMEOVER,
		WAIT
	}

	private enum Orientation {
		HORIZONTAL,
		VERTICAL
	}

	/**
	 * A thread  used for monitoring incoming traffic from the {@link server.JavashipsServer}
	 */
	class JavashipsNetworkingThread extends Thread {
		@Override
		public void run() {
			try {
				server = new Socket(host, port);

				commandWriter = new PrintWriter(
						server.getOutputStream(), true);

				commandReader =
						new BufferedReader(
								new InputStreamReader(
										server.getInputStream()
								));

				hasConnection = true;


			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}

			while (hasConnection) {
				String[] receivedParsed;
				try {
					String received = commandReader.readLine();
					receivedParsed = received.split(SEPARATOR);
				}
				catch (IOException e) {
					System.err.println(e.toString());
					System.out.println("Your opponent has disconnected.");
					chatArea.append("Your opponent has disconnected.\n");
					return;
				}

				System.out.print(new Date().toString() + " - Received");
				for (String s : receivedParsed) {
					System.out.print(" - ");
					System.out.print(s);
				}
				System.out.println();

				int x;
				int y;

				switch (receivedParsed[0]) {
					case MESSAGE:
						chatArea.append("Opponent: " + receivedParsed[1] + "\n");
						break;

					case FIRST:
						isFirstAttacker = true;
						break;

					case READY:
						opponentReady = true;
						checkStartGame();
						break;

					case SEEKING:
						x = Integer.parseInt(receivedParsed[1]);
						y = Integer.parseInt(receivedParsed[2]);

						PlayerButtons[x][y].setBackground(GridColor.SEEKING.c);
						break;

					case REDRAW:
						draw();
						break;

					case ATTACK:
						x = Integer.parseInt(receivedParsed[1]);
						y = Integer.parseInt(receivedParsed[2]);

						switch (PlayerGrid[x][y]) {
							case EMPTY:
								sendMiss(commandWriter, x, y);
								PlayerGrid[x][y] = GridValue.MISS;
								draw();
								break;
							default:
								String shipHit = PlayerGrid[x][y].toString();

								sendHit(commandWriter, x, y, PlayerGrid[x][y]);
								PlayerGrid[x][y] = GridValue.HIT;
								draw();

								if (shipHit.equals(GridValue.BATTLESHIP.toString())) {
									hpBATTLESHIP--;
								}
								else if (shipHit.equals(GridValue.CARRIER.toString())) {
									hpCARRIER--;
								}
								else if (shipHit.equals(GridValue.CRUISER.toString())) {
									hpCRUISER--;
								}
								else if (shipHit.equals(GridValue.DESTROYER.toString())) {
									hpDESTROYER--;
								}
								else if (shipHit.equals(GridValue.SUBMARINE.toString())) {
									hpSUBMARINE--;
								}
								else {
									try {
										throw new Exception("Ship hit is not known or wasn't sent!");
									}
									catch (Exception e) {
										e.printStackTrace();
									}
								}

								try {
									checkSunkenShips(shipHit);
								}
								catch (Exception e) {
									e.printStackTrace();
								}

								if (checkIfLost()) {
									sendGameOver(commandWriter);
									JOptionPane.showMessageDialog(frame, "You lost, nerd!", "GG!",
											JOptionPane.INFORMATION_MESSAGE);
									gameOver();
								}

								break;
						}

						gameState = GameState.PLAY;

						break;

					case HIT:
						x = Integer.parseInt(receivedParsed[1]);
						y = Integer.parseInt(receivedParsed[2]);

						String shipHit = receivedParsed[3];

						OppGrid[x][y] = GridValue.HIT;
						draw();

						try {
							chatArea.append("Opponent: You hit my " + shipHit + "!\n");
						}
						catch (Exception e) {
							e.printStackTrace();
						}

						break;

					case MISS:
						x = Integer.parseInt(receivedParsed[1]);
						y = Integer.parseInt(receivedParsed[2]);

						OppGrid[x][y] = GridValue.MISS;
						draw();

						break;

					case QUIT:
						quitGame();
						JOptionPane.showMessageDialog(frame,
								"Your opponent has quit the game. Sad!", "Game over",
								JOptionPane.INFORMATION_MESSAGE);
						break;

					case GAMEOVER:
						JOptionPane.showMessageDialog(frame,
								"You won!", "GG!",
								JOptionPane.INFORMATION_MESSAGE);
						gameOver();
						break;

					default:
						break;
				}
			}

		}
	}
}