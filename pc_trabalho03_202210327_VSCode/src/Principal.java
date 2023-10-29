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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
// Fim das bibliotecas importadas

public class Principal extends Application { // Classe Principal que herda da classe Application (JavaFX) para rodar a aplicacao
  // Variaveis globais da classe Principal
  private final static int N = 5; // Numero de filosofos
  private final static int HUNGRY = 1; // Estado "faminto"
  private final static int EATING = 2; // Estado "comendo"
  static Semaphore mutex = new Semaphore(1); // Semaforo para exclusão mutua
  static int state[] = new int[N]; // Array para o estado de cada filosofo
  static Semaphore[] s = new Semaphore[N]; // Array para o semaforo de cada filosofo

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
    primaryStage.getIcons().add(new Image("icon.png")); // Icone do aplicativo
    primaryStage.show(); // Apresenta a janela
    // Fim da instancia da tela Principal

    // Estrutura da GUI
    VBox mainVBox = new VBox(); // VBox principal (sera composta pelas HBoxes superior e inferior - area de visualizacao e controle dos filosofos)

    HBox topHBox = new HBox(); // HBox superior (area de visualizacao dos filosofos - divida em paineis esquerdo e direito)
    // Elementos da HBox superior
    Pane leftPane = new Pane(); // Painel/lado esquerdo (area de visualizacao dos filosofos)
    // Edicao do painel esquerdo
    leftPane.setPrefWidth(750); // Largura
    leftPane.setPrefHeight(580); // Altura
    leftPane.styleProperty().set("-fx-background-image: url('background-left.jpg');"); // Imagem de fundo
    // Fim da edicao do painel esquerdo

    Pane rightPane = new Pane(); // Painel/lado direito (area de informacoes e botao de Reset)
    // Edicao do painel direito
    rightPane.setPrefWidth(550); // Largura
    rightPane.setPrefHeight(580); // Altura
    rightPane.styleProperty().set("-fx-background-image: url('background-right.jpg');"); // Imagem de fundo

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
      double angle = (360 / N) * i; // Angulo seccionado pela quantidade de filosofo em relacao aA posicao circular ao redor da mesa "(360/N) * i"
      // Posicao X e Y de cada filosofo, calculada a partir do angulo multiplicado
      // pelo "raio" da mesa circular somado a um descolamento extra para melhor
      // centralizar os filosofos no lado esquerda)
      double x = 300 * Math.cos(Math.toRadians(angle)) + 260; // Posicao X
      double y = 200 * Math.sin(Math.toRadians(angle)) + 200; // Posicao Y
      // Fim do calculo de posicao

      // Estrutura da GUI de cada filosofo (filosofos e seus paineis ao lado esquerdo
      // e seus controles na parte inferior da tela)
      Pane paneFilosofo = createPhilosopherPane(i, x, y); // Painel de filosofo (criado pelo metodo createPhilosopherPane() com o id e as posicoes de cada filosofo calculadas anteriormente)
      VBox filosofoVBox = styledControlVBox("Controle do Filósofo " + i); // VBox de controle de cada filosofo (criada pelo metodo styledControlVBox())

      HBox botoes = new HBox(); // HBox principal dos botoes e sliders contidos na VBox de controle de cada filosofo
      // Estilizacao da HBox de botoes
      botoes.styleProperty()
          .set("-fx-spacing: 15px; -fx-padding: 5px; -fx-border-width: 2px; -fx-border-color: #d6ae69;");

      // Botoes Play/Pause de cada filosofo (inicia no valor "Pause" - filosofo rodando, clique para pausar)
      playOrPauseBTNs[i] = styledButton("Pause", 1, 0);
      playOrPauseBTNs[i].translateYProperty().set(30); // Posicionamento de cada botao Play/Pause (mais ao centro do eixo Y)

      VBox botoesVel = new VBox(); // VBox de sliders de controle ae velocidade pensar/comer
      botoesVel.styleProperty().set("-fx-alignment: center;"); // Estilizacao da VBox de sliders (alinhamento ao centro)

      VBox velBtnField1 = new VBox(); // VBox de controle de velocidade "pensar"
      Label velThinkText = new Label("Vel. Pensar (segundos)"); // Titulo da VBox de controle "pensar"
      // Estilizacao do titulo da VBox
      velThinkText.styleProperty().set("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #fff; -fx-text-alignment: center;");

      velSliderThinking[i] = createStyledSlider(0, 10, 5); // Slider de controle de velocidade "pensar"

      velBtnField1.getChildren().addAll(velThinkText, velSliderThinking[i]); // Adiciona o titulo e o slider de velocidade "pensar" na VBox de controle

      VBox velBtnField2 = new VBox(); // VBox de controle de velocidade "comer"
      Label velEatText = new Label("Vel. Comer (segundos)"); // Titulo da VBox de controle "comer"
      // Estilizacao do titulo da VBox
      velEatText.styleProperty().set("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #fff; -fx-text-alignment: center;");

      velSliderEating[i] = createStyledSlider(0, 10, 5); // Slider de controle de velocidade "comer"

      velBtnField2.getChildren().addAll(velEatText, velSliderEating[i]); // Adiciona o titulo e o slider de velocidade "comer" na VBox de controle

      botoesVel.getChildren().addAll(velBtnField1, velBtnField2); // Adiciona as VBox de controle de cada velocidade na VBox de controle de velocidades geral
      botoes.getChildren().addAll(playOrPauseBTNs[i], botoesVel); // Adiciona o botao Play/Pause e a VBox de controle de velocidades na HBox principal de botoes
      filosofoVBox.getChildren().add(botoes); // Adiciona a HBox principal de botoes na VBox de controle de cada filosofo (botoes ficam abaixo do titulo da VBox)

      leftPane.getChildren().addAll(paneFilosofo); // Adiciona o painel de cada filosofo no painel esquerdo principal
      bottomHBox.getChildren().addAll(filosofoVBox); // Adiciona a VBox de controle de cada filosofo na HBox inferior

      // Instancia dos Filosofos/Threads
      filosofos[i] = new Filosofo(paneFilosofo, i); // Instancia cada filosofo com o painel de cada filosofo e o id de cada filosofo
      filosofos[i].setThinkVelocity(5); // Velocidade de pensar padrao/inicial
      filosofos[i].setEatVelocity(5); // Velocidade de comer padrao/inicial

      s[i] = new Semaphore(0); // Inicializa os semaforos de cada filosofo para o controle na IPC

      filosofos[i].start(); // Inicia cada Thread ja ao iniciar a Aplicacao JavaFX
    } // Fim do loop de configuracao dos filosofos


    // Configuracoes e Eventos de Botoes e Sliders
    Button resetBTN = (Button) mainRVBox.getChildren().get(2); // Botao de resetar a simulacao (terceiro elemento da VBox principal do painel direito - sera criada pelo metodo createRightMainVBox() mais abaixo)

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

      for (int i = 0; i < N; i++) { // Loop para resetar configuracoes dos botoes Play/Pause e Sliders de cada filosofo
        if (playOrPauseBTNs[i].getText().equals("Play")) { // Se o filosofo estiver rodando no momento de click do Reset
          playOrPauseBTNs[i].fireEvent(e); // Dispara o evento que estiver configurado para cada botao de Play/Pause (sera configurado mais abaixo)
          /* [!] O evento para cada botao de Play/Pause refere-se ao evento de clique do
           * botao. Dessa forma, cada o filosofo esteja rodando no
           * momento do Reset, o evento de clique do botao de Play/Pause sera disparado,
           * pausando o filosofo (conforme a configuracao inicial da
           * Aplicacao).
           */
        }

        velSliderThinking[i].setValue(5); // Reinicializa o slider de velocidade de pensar
        velSliderEating[i].setValue(5); // Reinicializa o slider de velocidade de comer
      } // Fim do loop de resetar CONFIGURACOES de cada filosofo

      while (true) { // Loop para interromper a thread de cada filosofo
        for (int i = 0 ; i < N ; i++) {
          leftPane.getChildren().remove(filosofos[i].getFilosofoPane());
          filosofos[i].setRunningFlag(false);
          filosofos[i].interrupt();
          System.out.println("Thread do filosofo " + i + " interrupted: " + filosofos[i].isInterrupted());
        }
        break; // Somente apos interromper todas as threads, o loop sera interrompido e o codigo seguira adiante
      } // Fim do loop de interromper a thread de cada filosofo

      mutex = new Semaphore(1); // Redefine o semaforo para exclusão mutua
      state = new int[N]; // Reseta o estado de cada filosofo
      s = new Semaphore[N]; // Reseta o semaforo de cada filosofo

      for (int i = 0; i < N; i++) { // Loop para reconfigurar cada filosofo e seus respectivos recursos na GUI
        // Calcula novamente a posicao de cada filosofo na tela
        double angle = (360 / N) * i; // Angulo seccionado pela quantidade de filosofo em relacao aA posicao circular ao redor da mesa "(360/N) * i"
        // Posicao X e Y de cada filosofo, calculada a partir do angulo multiplicado
        // pelo "raio" da mesa circular somado a um descolamento extra para melhor
        // centralizar os filosofos no lado esquerda)
        double x = 300 * Math.cos(Math.toRadians(angle)) + 260; // Posicao X
        double y = 200 * Math.sin(Math.toRadians(angle)) + 200; // Posicao Y
        // Fim do calculo de posicao

        // Estrutura da GUI de cada filosofo (filosofos e seus paineis ao lado esquerdo
        // e seus controles na parte inferior da tela)
        Pane paneFilosofo = createPhilosopherPane(i, x, y); // Painel de filosofo (criado pelo metodo createPhilosopherPane() com o id e as posicoes de cada filosofo calculadas anteriormente)
        leftPane.getChildren().addAll(paneFilosofo); // Adiciona o "novo painel" de cada filosofo no painel esquerdo principal novamente

        filosofos[i] = new Filosofo(paneFilosofo, i); // Instancia cada filosofo novamente

        filosofos[i].setThinkVelocity(5); // Seta a velocidade de pensar padrao de cada filosofo
        filosofos[i].setEatVelocity(5); // Seta a velocidade de comer padrao de cada filosofo

        filosofos[i].start(); // Inicia a nova instancia de cada filosofo
        s[i] = new Semaphore(0); // Inicializa os novos semaforos de cada filosofo para o controle na IPC

        System.out.println("Filósofo " + i + " resetado e reiniciado."); // Mensagem de log (controle de execucao sem GUI)
      } // Fim do loop de resetar cada FILOSOFO
    }); // Fim do evento de clique do botao de resetar a simulacao

    for (int i = 0; i < N; i++) { // Loop para configurar os eventos de clique do botao Play/Pause de cada filosofo
      final int btnIndex = i; // Variavel final para o indice de cada botao de Play/Pause dentro do metodo setOnMouseClickedProperty()

      playOrPauseBTNs[i].onMouseClickedProperty().set(e -> { // A cada evento de clique sobre o botao de Play/Pause
        if (playOrPauseBTNs[btnIndex].getText().equals("Play")) { // Se o botao estiver com o texto "Play" (filosofo pausado que sera iniciado apos o click)
          playOrPauseBTNs[btnIndex].setText("Pause"); // Altera o texto do botao para "Pause" (pois o filosofo tera sido iniciado apos o click)

          playOrPauseBTNs[btnIndex].styleProperty().set( // Altera a estilizacao do botao de Play/Pause para o padrao "Pause"
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #cf0000;"); // Fim da alteracao no estilo do botao de Play/Pause para o padrao "Pause"

          playOrPauseBTNs[btnIndex].onMouseEnteredProperty().set(event -> { // Alteracao no estilo ao passar o mouse sobre o botao
            playOrPauseBTNs[btnIndex].cursorProperty().set(Cursor.HAND);
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #e80000;");
          }); // Fim da alteracao no estilo ao passar o mouse sobre o botao
          playOrPauseBTNs[btnIndex].onMouseExitedProperty().set(event -> { // Alteracao no estilo ao retirar o mouse do botao
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #cf0000;");
          }); // Fim da alteracao no estilo ao retirar o mouse do botao

          playOrPauseBTNs[btnIndex].onMousePressedProperty().set(event -> { // Alteracao no estilo ao pressionar o botao
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #b00000;");
          }); // Fim da alteracao no estilo ao pressionar o botao
          playOrPauseBTNs[btnIndex].onMouseReleasedProperty().set(event -> { // Alteracao no estilo ao soltar o botao
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #e80000;");
          }); // Fim da alteracao no estilo ao soltar o botao

          filosofos[btnIndex].resumeIt(); // "Despausa" cada filosofo com o metodo resumeIt() (pertence aA classe Filosofo)
          System.out.println("Filósofo " + btnIndex + " rodando."); // Mensagem de log (controle de execucao sem GUI)
        } else { // Se o botao estiver com o texto "Pause" (filosofo rodando que sera pausado apos o click)
          playOrPauseBTNs[btnIndex].setText("Play"); // Altera o texto do botao para "Play" (pois o filosofo tera sido pausado apos o click)

          playOrPauseBTNs[btnIndex].styleProperty().set( // Altera a estilizacao do botao de Play/Pause para o padrao "Play"
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00cf37;"); // Fim da alteracao no estilo do botao de Play/Pause para o padrao "Play"

          playOrPauseBTNs[btnIndex].onMouseEnteredProperty().set(event -> { // Alteracao no estilo ao passar o mouse sobre o botao
            playOrPauseBTNs[btnIndex].cursorProperty().set(Cursor.HAND);
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #00e029;");
          }); // Fim da alteracao no estilo ao passar o mouse sobre o botao
          playOrPauseBTNs[btnIndex].onMouseExitedProperty().set(event -> { // Alteracao no estilo ao retirar o mouse do botao
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #00cf37;");
          }); // Fim da alteracao no estilo ao retirar o mouse do botao

          playOrPauseBTNs[btnIndex].onMousePressedProperty().set(event -> { // Alteracao no estilo ao pressionar o botao
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00b02f;");
          }); // Fim da alteracao no estilo ao pressionar o botao
          playOrPauseBTNs[btnIndex].onMouseReleasedProperty().set(event -> { // Alteracao no estilo ao soltar o botao
            playOrPauseBTNs[btnIndex].styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00e029;");
          }); // Fim da alteracao no estilo ao soltar o botao

          filosofos[btnIndex].pauseIt(); // Pausa cada filosofo com o metodo pauseIt() (pertence aA classe Filosofo)
          System.out.println("Filósofo " + btnIndex + " pausado."); // Mensagem de log (controle de execucao sem GUI)
        } // Fim do if/else
      }); // Fim do evento de clique do botao de Play/Pause
    } // Fim do loop de configuracao dos eventos de clique do botao Play/Pause de cada filosofo

    for (int i = 0; i < N; i++) { // Loop para configurar os eventos de alteracao dos sliders de velocidade de pensar/comer de cada filosofo
      final int sliderIndex = i; // Variavel final para o indice de cada slider de velocidade de pensar/comer dentro do metodo valueProperty().addListener()

      velSliderThinking[i].valueProperty().addListener((observable, oldValue, newValue) -> { // A cada evento de alteracao do slider de velocidade de pensar
        if (newValue.intValue() > 0) { // Se o novo valor do slider for maior que 0 (impedindo que o filosofo "pense com velocidade 0")
          filosofos[sliderIndex].setThinkVelocity(newValue.intValue()); // Altera a velocidade de pensar do filosofo setando como o novo valor do slider
          System.out.println("Velocidade de pensar do filósofo " + sliderIndex + " alterada para "
              + (1 / newValue.intValue() * 10) + "."); // Mensagem de log (controle de execucao sem GUI)
        } // Fim do if
      }); // Fim do evento de alteracao do slider de velocidade de pensar

      velSliderEating[i].valueProperty().addListener((observable, oldValue, newValue) -> { // A cada evento de alteracao do slider de velocidade de comer
        if (newValue.intValue() > 0) { // Se o novo valor do slider for maior que 0 (impedindo que o filosofo "coma com velocidade 0")
          filosofos[sliderIndex].setEatVelocity(newValue.intValue()); // Altera a velocidade de comer do filosofo setando como o novo valor do slider
          System.out.println("Velocidade de comer do filósofo " + sliderIndex + " alterada para " + newValue.intValue() + "."); // Mensagem de log (controle de execucao sem GUI)
        } // Fim do if
      }); // Fim do evento de alteracao do slider de velocidade de comer
    } // Fim do loop de configuracao dos eventos de alteracao dos sliders de velocidade de pensar/comer de cada filosofo

    // Fim das configuracoes de Botoes/Sliders e Eventos de Botoes/Sliders
  } // Fim do metodo start

  /**
   * *************************************************************
   * Metodo: test
   * Funcao: testa se o filosofo pode comer com base no estado do filosofo a sua
   * esquerda e do filosofo a sua direita
   * Parametros: um inteiro representando o indice do filosofo que sera testado
   * Retorno: nao retorna valores
   ***************************************************************
   * @param i
   */
  public static void test(int i) {
    if (state[i] == HUNGRY && state[LEFT(i)] != EATING && state[RIGHT(i)] != EATING) { // Se o filosofo estiver faminto e os filosofos a sua esquerda e direita nao estiverem comendo
      try { // Tenta adquirir o semaforo do filosofo
        state[i] = EATING; // Altera o estado do filosofo para "comendo"
        s[i].release(); // Libera o semaforo do filosofo
      } catch (Exception e) { // Caso nao consiga adquirir o semaforo do filosofo
        e.printStackTrace(); // Imprime a pilha de exececoes
      } // Fim do try/catch
    } // Fim do if
  } // Fim do metodo test

  /**
   * *************************************************************
   * Metodo: left
   * Funcao: retorna o indice do filosofo a esquerda do filosofo passado como
   * parametro
   * Parametros: um inteiro representando o indice do filosofo que sera observado
   * Retorno: retorna um inteiro representando o indice do filosofo a esquerda do
   * filosofo passado como parametro
   ***************************************************************
   * @param i
   */
  public static int LEFT(int i) {
    return (i == 0) ? N - 1 : i - 1; // Se o filosofo for o primeiro (i == 0), retorna o ultimo filosofo. Caso contrario, retorna o filosofo a esquerda (i - 1)
  } // Fim do metodo LEFT

  /**
   * *************************************************************
   * Metodo: right
   * Funcao: retorna o indice do filosofo a direita do filosofo passado como
   * parametro
   * Parametros: um inteiro representando o indice do filosofo que sera observado
   * Retorno: retorna um inteiro representando o indice do filosofo a direita do
   * filosofo passado como parametro
   ***************************************************************
   * @param i
   */
  public static int RIGHT(int i) {
    return (i + 1) % N; // Retorna o indice do filosofo aA direita calculando o resto da divisao do indice do filosofo passado como parametro pelo numero de filosofos
  } // Fim do metodo RIGHT

  /**
   * *************************************************************
   * Metodo: createStyledSlider
   * Funcao: cria um slider estilizado que sera utilizado para controlar a
   * velocidade de pensar/comer de cada filosofo
   * Parametros: tres inteiros representando o valor minimo, maximo e inicial do
   * slider respectivamente
   * Retorno: retorna o slider estilizado e devidamente configurado
   ***************************************************************
   * @param min     valor minimo do slider
   * @param max     valor maximo do slider
   * @param initVal valor inicial do slider
   */
  public Slider createStyledSlider(int min, int max, int initVal) {
    Slider styledSlider = new Slider(min, max, initVal); // Instancia do slider com valores minimo, maximo e inicial

    styledSlider.setShowTickLabels(true); // Mostra os valores do slider
    styledSlider.setShowTickMarks(true); // Mostra as marcas do slider
    styledSlider.setMajorTickUnit(5); // Tamanho das marcas maiores do slider
    styledSlider.setMinorTickCount(5); // Tamanho de marcas menores do slider
    styledSlider.setBlockIncrement(1); // Define o incremento do slider (de 1 em 1)
    styledSlider.setPrefWidth(150); // Largura do slider
    styledSlider.setMaxHeight(0); // Altura do slider

    styledSlider.onMouseEnteredProperty().set(e -> { // Alteracao no estilo ao passar o mouse sobre o slider
      styledSlider.cursorProperty().set(Cursor.HAND); // Altera o cursor do mouse para a maozinha
    }); // Alteracao no estilo ao passar o mouse sobre o slider

    return styledSlider; // Retorna o slider estilizado
  } // Fim do metodo createStyledSlider

  /**
   * *************************************************************
   * Metodo: createPhilosopherPane
   * Funcao: cria um painel estilizado que sera utilizado para representar cada
   * filosofo
   * Parametros: um inteiro representando o indice do filosofo e dois doubles
   * representando as posicoes X e Y do painel do filosofo respectivamente
   * Retorno: retorna o painel estilizado e devidamente configurado
   ***************************************************************
   * @param i indice do filosofo
   * @param x posicao X do painel do filosofo
   * @param y posicao Y do painel do filosofo
   */
  public Pane createPhilosopherPane(int i, double x, double y) {
    Pane paneFilosofo = new Pane(); // Instancia do painel do filosofo
    paneFilosofo.styleProperty().set("-fx-background-image: url('filosofo" + i + ".png'); -fx-background-repeat: no-repeat;"); // Imagem de fundo do painel do filosofo
    paneFilosofo.setPrefWidth(180); // Largura do painel do filosofo
    paneFilosofo.setPrefHeight(180); // Altura do painel do filosofo
    paneFilosofo.setLayoutX(x); // Posicao X do painel do filosofo
    paneFilosofo.setLayoutY(y); // Posicao Y do painel do filosofo

    return paneFilosofo; // Retorna o painel estilizado
  } // Fim do metodo createPhilosopherPane

  /**
   * *************************************************************
   * Metodo: createRightMainVBox
   * Funcao: cria uma VBox estilizada que sera utilizada para representar o painel
   * direito principal da aplicacao
   * Parametros: nao possui parametros
   * Retorno: retorna a VBox estilizada e devidamente configurada
   ***************************************************************
   */
  public VBox createRightMainVBox() {
    VBox mainRVBox = new VBox(); // Instancia da VBox principal do lado direito
    mainRVBox.styleProperty().set("-fx-padding: 10px;"); // Estilo da VBox principal do lado direito

    Label mainRTitle = new Label(); // Titulo principal do lado direito
    mainRTitle.styleProperty().set("-fx-pref-width: 530px; -fx-pref-height: 80px; -fx-alignment: center; -fx-text-fill: #fff; -fx-font-size: 30px; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-image: url('mainRTitle.png'); -fx-background-repeat: no-repeat;"); // Estilo do titulo principal do lado direito
    mainRTitle.setTranslateY(20); // Posicionamento Y do titulo principal do lado direito

    Pane mainRTextPane = new Pane(); // Texto principal do lado direito
    mainRTextPane.styleProperty().set("-fx-pref-width: 470px; -fx-pref-height: 300px; -fx-background-image: url('mainRText.png'); -fx-background-repeat: no-repeat;"); // Estilo do texto principal do lado direito
    mainRTextPane.setTranslateX(40); // Posicionamento X do texto principal do lado direito
    mainRTextPane.setTranslateY(40); // Posicionamento Y do texto principal do lado direito

    Text mainRText = new Text("Bem-vindo(a) ao Jantar dos Filósofos!\nAcompanhe nossos pensadores enquanto\npensam e jantam ao lado.\n\nClique no botão abaixo\npara resetar a simulação e reiniciá-la!"); // Texto principal do lado direito
    mainRText.styleProperty().set("-fx-fill: #fff; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10px;"); // Estilo do texto principal do lado direito
    mainRText.textAlignmentProperty().set(javafx.scene.text.TextAlignment.CENTER); // Alinhamento do texto principal do lado direito
    mainRText.setTranslateX(40); // Posicionamento X do texto principal do lado direito
    mainRText.setTranslateY(75); // Posicionamento Y do texto principal do lado direito
    DropShadow shadow = new DropShadow(); // Sombra do texto principal do lado direito
    mainRText.setEffect(shadow); // Adiciona a sombra ao texto principal do lado direito
    mainRTextPane.getChildren().add(mainRText); // Adiciona o texto principal do lado direito no painel do texto principal do lado direito

    Button resetBTN = styledButton("Resetar\nSimulação", 0, 180); // Botao de resetar a simulacao
    resetBTN.textAlignmentProperty().set(javafx.scene.text.TextAlignment.CENTER); // Alinhamento do texto do botao de resetar a simulacao
    resetBTN.setTranslateX(180); // Posicionamento X do botao de resetar a simulacao
    resetBTN.setTranslateY(25); // Posicionamento Y do botao de resetar a simulacao

    mainRVBox.getChildren().addAll(mainRTitle, mainRTextPane, resetBTN); // Adiciona o titulo principal, o texto principal e os botoes na VBox principal do lado direito

    return mainRVBox; // Retorna a VBox estilizada
  } // Fim do metodo createRightMainVBox

  /**
   * *************************************************************
   * Metodo: styledControlVBox
   * Funcao: cria uma VBox estilizada que sera utilizada para representar o painel
   * de controle de cada filosofo (localizados na parte inferior da aplicacao)
   * Parametros: uma String representando o titulo principal da VBox
   * Retorno: retorna a VBox estilizada e devidamente configurada
   ***************************************************************
   * @param label titulo principal da VBox
   */
  public VBox styledControlVBox(String label) {
    VBox styledVBox = new VBox(); // Instancia da VBox
    Label mainTitle = new Label(label); // Titulo Principal da VBox
    mainTitle.styleProperty().set("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #fff; -fx-border-width: 2px; -fx-border-color: #d6ae69; -fx-background-color: #b59359; -fx-pref-width: 255px; -fx-alignment: center;"); // Estilo do titulo principal da VBox

    styledVBox.getChildren().addAll(mainTitle); // Adiciona o titulo principal na VBox principal
    styledVBox.styleProperty().set("-fx-background-image: url('bottom-header.jpg'); -fx-padding: 2px;"); // Estilo da VBox principal

    return styledVBox; // Retorna a VBox estilizada
  } // Fim do metodo styledControlVBox

  /**
   * *************************************************************
   * Metodo: styledButton
   * Funcao: cria um botao estilizado que sera utilizado para representar os
   * botoes de Play/Pause e Resetar Simulacao
   * Parametros: uma String representando o texto do botao, um inteiro
   * representando o tipo de botao e um inteiro representando a largura do botao
   * Retorno: retorna o botao estilizado e devidamente configurado
   ***************************************************************
   * @param text      texto do botao
   * @param type      tipo do botao
   * @param prefWidth largura do botao
   */
  public Button styledButton(String text, int type, int prefWidth) {
    Button styledButton = new Button(text); // Instancia do botao

    switch (type) { // Switch para configurar o estilo de cada botao a depender do tipo (0 = botao de Resetar Simulacao, 1 = botao de Play/Pause)
      case 1: // Botao de Play/Pause
        styledButton.setPrefWidth(65); // Largura do botao de Play/Pause

        if (text.equals("Play")) { // Configuracao do botao para o estado de "Play"
          styledButton.styleProperty().set( // Estilo do botao para o estado de "Play"
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00cf37;");

          styledButton.onMouseEnteredProperty().set(e -> { // Alteracao no estilo ao passar o mouse sobre o botao
            styledButton.cursorProperty().set(Cursor.HAND); // Altera o cursor do mouse para a maozinha
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #00e029;");
          }); // Fim da alteracao no estilo ao passar o mouse sobre o botao
          styledButton.onMouseExitedProperty().set(e -> { // Alteracao no estilo ao retirar o mouse do botao
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #00cf37;");
          }); // Fim da alteracao no estilo ao retirar o mouse do botao

          styledButton.onMousePressedProperty().set(e -> { // Alteracao no estilo ao pressionar o botao
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00b02f;");
          }); // Fim da alteracao no estilo ao pressionar o botao
          styledButton.onMouseReleasedProperty().set(e -> { // Alteracao no estilo ao soltar o botao
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #00b02f; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #00e029;");
          }); // Fim da alteracao no estilo ao soltar o botao

        } else if (text.equals("Pause")) { // Configuracao do botao para o estado de "Pause"
          styledButton.styleProperty().set( // Estilo do botao para o estado de "Pause"
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #cf0000;");

          styledButton.onMouseEnteredProperty().set(e -> { // Alteracao no estilo ao passar o mouse sobre o botao
            styledButton.cursorProperty().set(Cursor.HAND); // Altera o cursor do mouse para a maozinha
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #e80000;");
          }); // Fim da alteracao no estilo ao passar o mouse sobre o botao
          styledButton.onMouseExitedProperty().set(e -> { // Alteracao no estilo ao retirar o mouse do botao
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #cf0000;");
          }); // Fim da alteracao no estilo ao retirar o mouse do botao

          styledButton.onMousePressedProperty().set(e -> { // Alteracao no estilo ao pressionar o botao
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #b00000;");
          }); // Fim da alteracao no estilo ao pressionar o botao
          styledButton.onMouseReleasedProperty().set(e -> { // Alteracao no estilo ao soltar o botao
            styledButton.styleProperty().set(
                "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #b00000; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #e80000;");
          }); // Fim da alteracao no estilo ao soltar o botao
        } // Fim do if/else

        break; // Fim do case 1
      default: // Botao de Resetar Simulacao
        styledButton.setPrefWidth(prefWidth); // Largura do botao de Resetar Simulacao (definida pelo parametro)

        styledButton.styleProperty().set( // Estilo do botao de Resetar Simulacao
            "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #fcf803; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #cf0000;");

        styledButton.onMouseEnteredProperty().set(e -> { // Alteracao no estilo ao passar o mouse sobre o botao
          styledButton.cursorProperty().set(Cursor.HAND); // Altera o cursor do mouse para a maozinha
          styledButton.styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #fcf803; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #e80000;");
        }); // Fim da alteracao no estilo ao passar o mouse sobre o botao
        styledButton.onMouseExitedProperty().set(e -> { // Alteracao no estilo ao retirar o mouse do botao
          styledButton.styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border: 5px; -fx-border-width: 2px; -fx-border-color: #fcf803; -fx-border-radius: 5px;  -fx-background-radius: 5px; -fx-background-color: #cf0000;");
        }); // Fim da alteracao no estilo ao retirar o mouse do botao

        styledButton.onMousePressedProperty().set(e -> { // Alteracao no estilo ao pressionar o botao
          styledButton.styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #fcf803; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #b00000;");
        }); // Fim da alteracao no estilo ao pressionar o botao
        styledButton.onMouseReleasedProperty().set(e -> { // Alteracao no estilo ao soltar o botao
          styledButton.styleProperty().set(
              "-fx-font-size: 15px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-width: 2px; -fx-border-color: #fcf803; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #e80000;");
        }); // Fim da alteracao no estilo ao soltar o botao

        break; // Fim do default
    } // Fim do switch

    return styledButton; // Retorna o botao estilizado
  } // Fim do metodo styledButton

  /**
   * *************************************************************
   * Metodo: main
   * Funcao: metodo principal da aplicacao
   * Parametros: um array de Strings representando os argumentos passados para a
   * aplicacao
   * Retorno: nao retorna valores
   ***************************************************************
   * @param args argumentos passados para a aplicacao
   */
  public static void main(String[] args) {
    launch(args); // Inicia a aplicacao
  } // Fim do metodo main
} // Fim da classe Principal