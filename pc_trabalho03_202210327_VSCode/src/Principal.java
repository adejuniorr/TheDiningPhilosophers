/* ***************************************************************
* Autor............: Ademir de Jesus Reis Junior
* Matricula........: 202210327
* Inicio...........: 23/10/2023
* Ultima alteracao.: 28/10/2023
* Nome.............: Principal.java
* Funcao...........: Aplicacao JavaFX que simula o problema de IPC (Interprocess Comunication) "Jantar dos Filosofos"
*************************************************************** */

// Bibliotecas importadas para o funcionamento da logica e interface grafica do programa
import java.util.concurrent.Semaphore;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
// Fim das bibliotecas importadas

public class Principal extends Application { // Classe Principal que herda da classe Application (JavaFX) para rodar a aplicacao
  // Variaveis globais da classe Principal
  private final static int N = 5; // Numero de filosofos
  private final static int HUNGRY = 1; // Estado "faminto"
  private final static int EATING = 2; // Estado "comendo"
  static Semaphore mutex = new Semaphore(1); // Semáforo para exclusão mútua

  static int state[] = new int[N]; // Array para o estado de cada filósofo
  static Semaphore[] s = new Semaphore[N]; // Array para o semáforo de cada filósofo

  /**
   * *************************************************************
   * Metodo: start (sobrescrito da classe Application)
   * Funcao: inicializa a aplicacao JavaFX
   * Parametros: primaryStage (Stage)
   * Retorno: nao retorna valores
   ***************************************************************
   * @param primaryStage
   */
  @Override
  public void start(Stage primaryStage) throws Exception {
    // Instancia da tela Principal
    Pane root = new Pane(); // Painel raiz
    Scene scene = new Scene(root, 1300, 744); // Cena principal

    primaryStage.setTitle("O Jantar dos Filósofos"); // Titulo da janela
    primaryStage.setScene(scene); // Adiciona a cena princiapl aA janela
    primaryStage.setResizable(false); // Janela nao redimensionavel
    primaryStage.centerOnScreen(); // Janela inicia no centro da tela
    primaryStage.getIcons().add(new Image("filosofo4.png")); // Icone do aplicativo
    primaryStage.show(); // Apresenta a janela
    // Fim da instancia da tela Principal

    // Estrutura da GUI
    VBox mainVBox = new VBox(); // VBox principal (sera composta pelas HBoxes superior e inferior - area de visualizacao e controle dos filosofos)
    
    HBox topHBox = new HBox(); // HBox superior (area de visualizacao dos filosofos - divida em paineis esquerdo e direito)
    // Elementos da HBox superior
    Pane leftPane = new Pane(); // Painel/lado esquerdo (area de visualizacao dos filosofos)
    // Edicao do painel esquerdo
    leftPane.setPrefWidth(600); // Largura
    leftPane.setPrefHeight(580); // Altura
    leftPane.styleProperty().set("-fx-background-image: url('background-left.jpg');"); // Imagem de fundo
    // Fim da edicao do painel esquerdo

    Pane rightPane = new Pane(); // Painel/lado direito (area de informacoes e botao de Reset)
    // Edicao do painel direito
    rightPane.setPrefWidth(700); // Largura
    rightPane.setPrefHeight(580); // Altura
    rightPane.styleProperty().set("-fx-background-image: url('background-right2.jpg');"); // Imagem de fundo
    
    VBox mainRVBox = createRightMainVBox(); // VBox principal do painel direito (criada pelo metodo createRightMainVBox())
    rightPane.getChildren().add(mainRVBox); // Adiciona a VBox principal no painel direito
    // Fim da edicao do painel direito

    topHBox.getChildren().addAll(leftPane, rightPane); // Adiciona os paineis esquerdo e direito na HBox superior
    // Fim das configs e edits dos elementos da HBox superior
    
    HBox bottomHBox = new HBox(); // HBox inferior (area de controle dos filosofos - ira conter as VBoxes de controle de cada filosofo)

    mainVBox.getChildren().addAll(topHBox, bottomHBox); // Adiciona as HBoxes (superior e inferior) na VBox principal
    root.getChildren().add(mainVBox); // Adiciona a VBox principal ao painel raiz

    Slider[] velSliderThinking = new Slider[N]; // Array de sliders para controlar a velocidade de pensar
    Slider[] velSliderEating = new Slider[N]; // Array de sliders para controlar a velocidade de comer
    Button[] playOrPauseBTNs = new Button[N]; // Array de botoes para controlar o estado de cada filosofo (Play/Pause)

    // Configuracoes dos Filosofos
    Filosofo[] filosofos = new Filosofo[N]; // Array de filosofos

    for (int i = 0; i < N; i++) { // Loop para instanciar, configurar e iniciar cada filosofo e seus recursos
      // Calcula a posicao de cada filosofo na tela
      double angle = (360 / N) * i;
      double x = 230 * Math.cos(Math.toRadians(angle)) + 240;
      double y = 230 * Math.sin(Math.toRadians(angle)) + 210;
      // Fim do calculo de posicao

      // Estrutura da GUI de cada filosofo (filosofos e seus paineis ao lado esquerdo e seus controles na parte inferior da tela)
      Pane paneFilosofo = createPhilosopherPane(i, x, y); // Painel de filosofo (criado pelo metodo createPhilosopherPane() com o id e as posicoes de cada filosofo calculadas anteriormente)
      VBox filosofoVBox = styledControlVBox("Controle do Filósofo " + i); // VBox de controle de cada filosofo (criada pelo metodo styledControlVBox())

      HBox botoes = new HBox(); // HBox principal dos botoes e sliders contidos na VBox de controle de cada filosofo
      // Estilizacao da HBox de botoes
      botoes.styleProperty().set("-fx-spacing: 15px; -fx-padding: 5px; -fx-border-width: 2px; -fx-border-color: #d6ae69;");
      
      // Botoes Play/Pause de cada filosofo (inicia no valor "Pause" - filosofo rodando, clique para pausar)
      playOrPauseBTNs[i] = styledButton("Pause", 1, 0); 
      playOrPauseBTNs[i].translateYProperty().set(30); // Posicionamento de cada botao Play/Pause (mais ao centro do eixo Y)

      VBox botoesVel = new VBox(); // VBox de sliders de controle ae velocidade pensar/comer
      botoesVel.styleProperty().set("-fx-alignment: center;"); // Estilizacao da VBox de sliders (alinhamento ao centro)

      VBox velBtnField1 = new VBox(); // VBox de controle de velocidade "pensar"
      Label velThinkText = new Label("Vel. Pensar"); // Titulo da VBox de controle "pensar"
      // Estilizacao do titulo da VBox
      velThinkText.styleProperty().set("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #fff; -fx-text-alignment: center;");

      // Slider de controle de velocidade "pensar"
      velSliderThinking[i] = createStyledSlider(1, 10, 5); 

      velBtnField1.getChildren().addAll(velThinkText, velSliderThinking[i]); // Adiciona o titulo e o slider de velocidade "pensar" na VBox de controle

      VBox velBtnField2 = new VBox(); // VBox de controle de velocidade "comer"
      Label velEatText = new Label("Vel. Comer"); // Titulo da VBox de controle "comer"
      // Estilizacao do titulo da VBox
      velEatText.styleProperty().set("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #fff; -fx-text-alignment: center;");

      // Slider de controle de velocidade "comer"
      velSliderEating[i] = createStyledSlider(1, 10, 5);

      velBtnField2.getChildren().addAll(velEatText, velSliderEating[i]); // Adiciona o titulo e o slider de velocidade "comer" na VBox de controle

      botoesVel.getChildren().addAll(velBtnField1, velBtnField2); // Adiciona as VBox de controle de cada velocidade na VBox de controle de velocidades geral
      botoes.getChildren().addAll(playOrPauseBTNs[i], botoesVel); // Adiciona o botao Play/Pause e a VBox de controle de velocidades na HBox principal de botoes
      filosofoVBox.getChildren().add(botoes); // Adiciona a HBox principal de botoes na VBox de controle de cada filosofo (botoes ficam abaixo do titulo da VBox)

      leftPane.getChildren().addAll(paneFilosofo); // Adiciona o painel de cada filosofo no painel esquerdo principal
      bottomHBox.getChildren().addAll(filosofoVBox); // Adiciona a VBox de controle de cada filosofo na HBox inferior

      // Instancia dos Filosofos/Threads
      filosofos[i] = new Filosofo(paneFilosofo, i);
      filosofos[i].setThinkVelocity(5); // Velocidade de pensar padrao/inicial
      filosofos[i].setEatVelocity(5); // Velocidade de comer padrao/inicial
      filosofos[i].start(); // Inicia cada Thread ja ao iniciar a Aplicacao JavaFX

      s[i] = new Semaphore(0); // Inicializa os semaforos de cada filosofo para o controle na IPC
    } // Fim do loop de configuracao dos filosofos

    // Configuracoes de Botoes/Sliders e Eventos de Botoes/Sliders
    Button resetBTN = (Button) mainRVBox.getChildren().get(2); // Botao de resetar a simulacao

    /**
     * *************************************************************
     * Evento/Propriedade: onMouseClicked
     * Funcao: identifica qualquer clique sobre o botao de resetar a simulacao
     * Parametros/set: e (instancia de MouseEvent)
     * Retorno: nao retorna valores
     ***************************************************************
     * @param e
     */
    resetBTN.onMouseClickedProperty().set(e -> { // A cada evento de clique do botao de resetar a simulacao
      System.out.println("Simulação resetada!"); // Mensagem de log (controle de execucao sem GUI)
      
      for (int i = 0; i < N; i++) { // Loop para resetar CONFIGURACOES de cada filosofo
        s[i] = new Semaphore(0); // Reseta o semaforo de cada filosofo
        
        if (playOrPauseBTNs[i].getText().equals("Play")) { // Se o filosofo estiver rodando no momento de click do Reset
          playOrPauseBTNs[i].fireEvent(e); // Dispara o evento que estiver configurado para cada botao de Play/Pause (sera configurado mais abaixo)
          /* [!] O evento para cada botao de Play/Pause refere-se ao evento de clique do botao. Dessa forma, cada o filosofo esteja rodando no
           * momento do Reset, o evento de clique do botao de Play/Pause sera disparado, pausando o filosofo (conforme a configuracao inicial da 
           * Aplicacao).
           */
        }

        velSliderThinking[i].setValue(5); // Reinicializa o slider de velocidade de pensar
        velSliderEating[i].setValue(5); // Reinicializa o slider de velocidade de comer
      } // Fim do loop de resetar CONFIGURACOES de cada filosofo

      for (int i = 0; i < N; i++) { // Loop para resetar cada FILOSOFO
        filosofos[i].interrupt(); // Interrompe a Thread Filosofo
        System.out.println("Thread do filosofo " + i + " interrupted: " + filosofos[i].isInterrupted()); // Mensagem de log (controle de execucao sem GUI)

        filosofos[i] = new Filosofo(filosofos[i].getFilosofoPane(), i); // Instancia cada filosofo novamente
        filosofos[i].setThinkVelocity(5); // Seta a velocidade de pensar de cada filosofo
        filosofos[i].setEatVelocity(5); // Seta a velocidade de comer de cada filosofo
        filosofos[i].start(); // Inicia a nova instancia de cada filosofo

        // Redefine o painel padrao de cada filosofo (conforme a configuracao inicial da Aplicacao)
        filosofos[i].getFilosofoPane().styleProperty().set("-fx-background-image: url('filosofo" + i + ".png'); -fx-background-repeat: no-repeat;");
        
        System.out.println("Filósofo " + i + " resetado e reiniciado."); // Mensagem de log (controle de execucao sem GUI)
      }
    });

    for (int i = 0; i < N; i++) {
      final int btnIndex = i;

      playOrPauseBTNs[i].onMouseClickedProperty().set(e -> {
        if (playOrPauseBTNs[btnIndex].getText().equals("Play")) {
          playOrPauseBTNs[btnIndex].setText("Pause");

          playOrPauseBTNs[btnIndex].styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #cf0000;");

          playOrPauseBTNs[btnIndex].onMouseEnteredProperty().set(event -> {
            playOrPauseBTNs[btnIndex].cursorProperty().set(Cursor.HAND);
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #e80000;");
          });
          playOrPauseBTNs[btnIndex].onMouseExitedProperty().set(event -> {
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #cf0000;");
          });

          playOrPauseBTNs[btnIndex].onMousePressedProperty().set(event -> {
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #b00000;");
          });
          playOrPauseBTNs[btnIndex].onMouseReleasedProperty().set(event -> {
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #e80000;");
          });

          filosofos[btnIndex].resumeIt(); // (re)Inicia cada filosofo
          System.out.println("Filósofo " + btnIndex + " rodando.");
        } else {
          playOrPauseBTNs[btnIndex].setText("Play");

          playOrPauseBTNs[btnIndex].styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00cf37;");

          playOrPauseBTNs[btnIndex].onMouseEnteredProperty().set(event -> {
            playOrPauseBTNs[btnIndex].cursorProperty().set(Cursor.HAND);
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #00e029;");
          });
          playOrPauseBTNs[btnIndex].onMouseExitedProperty().set(event -> {
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #00cf37;");
          });

          playOrPauseBTNs[btnIndex].onMousePressedProperty().set(event -> {
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00b02f;");
          });
          playOrPauseBTNs[btnIndex].onMouseReleasedProperty().set(event -> {
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00e029;");
          });

          filosofos[btnIndex].pauseIt(); // Pausa cada filosofo
          System.out.println("Filósofo " + btnIndex + " pausado.");
        }
      });
    }

    for (int i = 0; i < N; i++) {
      final int sliderIndex = i;

      velSliderThinking[i].valueProperty().addListener((observable, oldValue, newValue) -> {
        filosofos[sliderIndex].setThinkVelocity(newValue.intValue()); // Altera a velocidade de pensar do filosofo
                                                                      // [sliderIndex]
        // System.out.println("Velocidade de pensar do filósofo " + sliderIndex + "
        // alterada para " + (1/newValue.intValue()*10) + ".");

      });

      velSliderEating[i].valueProperty().addListener((observable, oldValue, newValue) -> {
        filosofos[sliderIndex].setEatVelocity(newValue.intValue()); // Altera a velocidade de comer do filosofo
                                                                    // [sliderIndex]
        // System.out.println("Velocidade de comer do filósofo " + sliderIndex + "
        // alterada para " + newValue.intValue() + ".");
      });
    }
    // Fim de eventos
  }

  public static void test(int i) { // Testa se o filosofo pode comer
    if (state[i] == HUNGRY && state[LEFT(i)] != EATING && state[RIGHT(i)] != EATING) {
      try {
        state[i] = EATING;
        s[i].release();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static int LEFT(int i) {
    return (i == 0) ? N - 1 : i - 1;
  }

  public static int RIGHT(int i) {
    return (i + 1) % N;
  }

  public Slider createStyledSlider(int min, int max, int initVal) {
    Slider styledSlider = new Slider(min, max, initVal); // Instancia do slider com valores minimo, maximo e inicial

    styledSlider.setShowTickLabels(true); // Mostra os valores do slider
    styledSlider.setShowTickMarks(true); // Mostra as marcas do slider
    styledSlider.setMajorTickUnit(5); // Tamanho das marcas maiores do slider
    styledSlider.setMinorTickCount(5); // Tamanho de marcas menores do slider
    styledSlider.setBlockIncrement(1);
    styledSlider.setPrefWidth(150); // Largura do slider
    styledSlider.setMaxHeight(0); // Altura do slider

    return styledSlider;
  }

  public Pane createPhilosopherPane(int i, double x, double y) {
    Pane paneFilosofo = new Pane();
    paneFilosofo.setPrefWidth(130);
    paneFilosofo.setPrefHeight(180);

    paneFilosofo.styleProperty()
        .set("-fx-background-image: url('filosofo" + i + ".png'); -fx-background-repeat: no-repeat;");
    paneFilosofo.setLayoutX(x);
    paneFilosofo.setLayoutY(y);

    return paneFilosofo;
  }

  public VBox createRightMainVBox() {
    VBox mainRVBox = new VBox(); // VBox principal do lado direito
    mainRVBox.styleProperty().set("-fx-padding: 10px;");
    Label mainRTitle = new Label("O Jantar dos Filósofos"); // Titulo principal do lado direito
    mainRTitle.styleProperty().set(
        "-fx-pref-width: 680px; -fx-alignment: center; -fx-text-fill: #fff; -fx-font-size: 30px; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-color: #b59359;"); // Estilo
                                                                                                                                                                             // do
                                                                                                                                                                             // titulo
                                                                                                                                                                             // principal
                                                                                                                                                                             // do
                                                                                                                                                                             // lado
                                                                                                                                                                             // direito

    Label mainRText = new Label(
        "\nOlá!\nEsta é uma simulação que retrata o problema de programação do Jantar dos Filósofos.\nAqui observamos o acesso a recursos compartilhados e a comunicação entre processos na prática, sem a ocorrência de Condições de Corrida ao acessar uma mesma Região Crítica.\n\nClique no botão abaixo para iniciar a simulação!"); // Texto
                                                                                                                                                                                                                                                                                                                                          // principal
                                                                                                                                                                                                                                                                                                                                          // do
                                                                                                                                                                                                                                                                                                                                          // lado
                                                                                                                                                                                                                                                                                                                                          // direito
    mainRText.styleProperty().set(
        "-fx-pref-width: 620px; -fx-alignment: center; -fx-text-fill: #fff; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10px;"); // Estilo
                                                                                                                                              // do
                                                                                                                                              // texto
                                                                                                                                              // principal
                                                                                                                                              // do
                                                                                                                                              // lado
                                                                                                                                              // direito
    mainRText.setTranslateX(30);
    mainRText.textAlignmentProperty().set(javafx.scene.text.TextAlignment.CENTER); // Alinhamento do texto principal do
                                                                                   // lado direito
    mainRText.wrapTextProperty().set(true);

    Button resetBTN = styledButton("Resetar\nSimulação", 0, 180); // Botao de resetar a simulacao
    resetBTN.textAlignmentProperty().set(javafx.scene.text.TextAlignment.CENTER); // Alinhamento do botao de resetar a
                                                                                  // simulacao
    resetBTN.setTranslateX(260);
    resetBTN.setTranslateY(50);

    mainRVBox.getChildren().addAll(mainRTitle, mainRText, resetBTN); // Adiciona o titulo principal, o texto principal e
                                                                     // os botoes na VBox principal do lado direito

    return mainRVBox;
  }

  public VBox styledControlVBox(String label) {
    VBox styledVBox = new VBox(); // Instancia da VBox
    Label mainTitle = new Label(label); // Titulo Principal da VBox
    mainTitle.styleProperty().set(
        "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #fff; -fx-border-width: 2px; -fx-border-color: #d6ae69; -fx-background-color: #b59359; -fx-pref-width: 255px; -fx-alignment: center;"); // Estilo
                                                                                                                                                                                                            // do
                                                                                                                                                                                                            // titulo
                                                                                                                                                                                                            // principal

    styledVBox.getChildren().addAll(mainTitle); // Adiciona o titulo principal e a VBox de botoes na VBox principal
    styledVBox.styleProperty().set("-fx-background-image: url('bottom-header.jpg'); -fx-padding: 2px;");

    return styledVBox;
  }

  public Button styledButton(String text, int type, int prefWidth) {
    Button styledButton = new Button(text);
    styledButton.onMouseEnteredProperty().set(e -> {
      styledButton.cursorProperty().set(Cursor.HAND);
    });

    switch (type) {
      case 1: // Botao de Play/Pause
        styledButton.setPrefWidth(65);

        if (text.equals("Play")) {
          styledButton.styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00cf37;");

          styledButton.onMouseEnteredProperty().set(e -> {
            styledButton.cursorProperty().set(Cursor.HAND);
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #00e029;");
          });
          styledButton.onMouseExitedProperty().set(e -> {
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #00cf37;");
          });

          styledButton.onMousePressedProperty().set(e -> {
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00b02f;");
          });
          styledButton.onMouseReleasedProperty().set(e -> {
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00e029;");
          });

        } else if (text.equals("Pause")) {
          styledButton.styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #cf0000;");

          styledButton.onMouseEnteredProperty().set(e -> {
            styledButton.cursorProperty().set(Cursor.HAND);
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #e80000;");
          });
          styledButton.onMouseExitedProperty().set(e -> {
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #cf0000;");
          });

          styledButton.onMousePressedProperty().set(e -> {
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #b00000;");
          });
          styledButton.onMouseReleasedProperty().set(e -> {
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #e80000;");
          });
        }

        break;
      default: // Outros botoes (reset, etc.)
        styledButton.setPrefWidth(prefWidth);

        styledButton.styleProperty().set(
            "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 5px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #0070ad; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #0086cf;");

        styledButton.onMouseEnteredProperty().set(e -> {
          styledButton.cursorProperty().set(Cursor.HAND);
          styledButton.styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 5px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #0070ad; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #009bf0;");
        });
        styledButton.onMouseExitedProperty().set(e -> {
          styledButton.styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 5px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #0070ad; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #0086cf;");
        });

        styledButton.onMousePressedProperty().set(e -> {
          styledButton.styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 5px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #009bf0; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #0070ad;");
        });
        styledButton.onMouseReleasedProperty().set(e -> {
          styledButton.styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 5px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #0070ad; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #009bf0;");
        });

        break;
    }

    return styledButton;
  }

  /*
   * public void eatingImg(Filosofo filosofo) {
   * int filosofoId = filosofo.getIdFilosofo();
   * Pane filosofoPane = filosofo.getFilosofoPane();
   * 
   * switch (filosofoId) {
   * case 1:
   * filosofoPane.styleProperty().
   * set("-fx-background-image: url('filosofo1-comendo.png');");
   * break;
   * case 2:
   * filosofoPane.styleProperty().
   * set("-fx-background-image: url('filosofo2-comendo.png');");
   * break;
   * case 3:
   * filosofoPane.styleProperty().
   * set("-fx-background-image: url('filosofo3-comendo.png');");
   * break;
   * case 4:
   * filosofoPane.styleProperty().
   * set("-fx-background-image: url('filosofo4-comendo.png');");
   * break;
   * case 5:
   * filosofoPane.styleProperty().
   * set("-fx-background-image: url('filosofo5-comendo.png');");
   * break;
   * default:
   * break;
   * }
   * }
   */

  public static void main(String[] args) {
    launch(args);
  }
}