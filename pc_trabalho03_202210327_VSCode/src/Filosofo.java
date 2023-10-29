/* ***************************************************************
* Autor............: Ademir de Jesus Reis Junior
* Matricula........: 202210327
* Inicio...........: 23/10/2023
* Ultima alteracao.: 28/10/2023
* Nome.............: Filosofo.java
* Funcao...........: Classe Filosofo que herda de Thread
*************************************************************** */

// Bibliotecas importadas
import javafx.scene.layout.Pane;

public class Filosofo extends Thread { // Classe Filosofo que herda de Thread
  // Atributos da classe
  private Pane filosofoPane; // Pane que representa o filosofo
  private int id; // Identificador do filosofo
  private int thinkVelocity; // Velocidade de pensamento do filosofo
  private int eatVelocity; // Velocidade de comer do filosofo
  private volatile boolean running = true; // Flag que indica se o filosofo esta em execucao

  /**
   * *************************************************************
   * Construlor
   * Funcao: instancia o objeto da classe Filosofo
   * Parametros: um elemento Pane que representa o filosofo e um inteiro representando o id do filosofo
   * Retorno: nao retorna valor
   ***************************************************************
   * @param filosofoPane elemento Pane que representa o filosofo
   * @param id id do filosofo
   */
  public Filosofo(Pane filosofoPane, int id) {
    this.filosofoPane = filosofoPane;
    this.id = id;
  }

  // Metodos
  /**
   * *************************************************************
   * Metodo: run
   * Funcao: executa a thread do filosofo
   * Parametros: nao recebe parametros
   * Retorno: nao retorna valor
   *************************************************************** 
   */
  @Override
  public void run() {
    while (true) { // Loop infinito que testara se a thread foi pausada e/ou interrompida

      while (running) { // Loop que executa o filosofo enquanto a flag running for true (nao pausado)
        think(); // Filosofo pensa
        try {
          getForks(); // Filosofo pega os garfos
          eat(); // Filosofo come
          putForks(); // Filosofo devolve os garfos
        } catch (InterruptedException e) { // Tratamento de excecao caso a thread seja interrompida durante a execucao de um dos metodos anteriores
          Thread.currentThread().interrupt(); // Interrompe a thread de fato
          break; // Sai do loop
        } // Fim do try-catch
      } // Fim do loop while (running)

      if (Thread.currentThread().isInterrupted()) { // Testa se a thread foi interrompida
        break; // Sai do loop caso a thread tenha sido interrompida
      } // Fim do if

    } // Fim do loop while (true)
  } // Fim do metodo run

  /**
   * *************************************************************
   * Metodo: pauseIt
   * Funcao: pausa a execucao da thread do filosofo
   * Parametros: nao recebe parametros
   * Retorno: nao retorna valor
   *************************************************************** */
  public void pauseIt() {
    running = false; // Altera a flag running para false (pausado)
  } // Fim do metodo pauseIt

  /**
   * *************************************************************
   * Metodo: resumeIt
   * Funcao: retoma a execucao da thread do filosofo
   * Parametros: nao recebe parametros
   * Retorno: nao retorna valor
   *************************************************************** */
  public void resumeIt() {
    running = true; // Altera a flag running para true (retomado)
  } // Fim do metodo resumeIt

  /**
   * *************************************************************
   * Metodo: think
   * Funcao: mantem a thread em sleep por um tempo (representando o filosofo pensando)
   * Parametros: nao recebe parametros
   * Retorno: nao retorna valor
   *************************************************************** */
  public void think() {
    if (running) { // Testa se a flag running esta true (nao pausado)
      try {
        filosofoPane.styleProperty().set("-fx-background-image: url('filosofo" + id + "-pensando.png'); -fx-background-repeat: no-repeat;"); // Altera a imagem do filosofo para a imagem de pensando
        
        long time = getThinkVelocity(); // Obtem o tempo de pensamento do filosofo (o padrao ou o setado pelo Slider na GUI da classe Principal)
        System.out.println("Filósofo " + id + " está pensando por " + time + " segundos."); // Imprime no console o tempo que o filosofo levara para pensar
        Thread.sleep(time*1000); // Mantem a thread em sleep por um tempo (representando o filosofo pensando)

      } catch (InterruptedException e) { // Tratamento de excecao caso a thread seja interrompida durante a execucao do metodo
        Thread.currentThread().interrupt(); // Interrompe a thread de fato
      } // Fim do try-catch
    } // Fim do if
  } // Fim do metodo think

  /**
   * *************************************************************
   * Metodo: eat
   * Funcao: mantem a thread em sleep por um tempo (representando o filosofo comendo)
   * Parametros: nao recebe parametros
   * Retorno: nao retorna valor
   *************************************************************** */
  public void eat() {
    if (running) { // Testa se a flag running esta true (nao pausado)
      try {
        filosofoPane.styleProperty().set("-fx-background-image: url('filosofo" + id + "-comendo.png'); -fx-background-repeat: no-repeat;"); // Altera a imagem do filosofo para a imagem de comendo
        
        long time = getEatVelocity(); // Obtem o tempo de comendo do filosofo (o padrao ou o setado pelo Slider na GUI da classe Principal)
        System.out.println("Filósofo " + id + " está comendo por " + time + " segundos."); // Imprime no console o tempo que o filosofo levara para comer
        Thread.sleep(time*1000); // Mantem a thread em sleep por um tempo (representando o filosofo comendo)

      } catch (InterruptedException e) { // Tratamento de excecao caso a thread seja interrompida durante a execucao do metodo
        Thread.currentThread().interrupt(); // Interrompe a thread de fato
      } // Fim do try-catch
    } // Fim do if
  } // Fim do metodo eat

  /**
   * *************************************************************
   * Metodo: getForks
   * Funcao: altera o estado do semaforo mutex para 1 e testa se o filosofo pode pegar os garfos. Caso possa, altera o estado do semaforo para 0 e o filosofo tem seu proprio estado alterado para 1 (comendo)
   * Parametros: nao recebe parametros
   * Retorno: nao retorna valor
   *************************************************************** */
  public void getForks() {
    try {
      if (running) { // Testa se a flag running esta true (nao pausado)
        Principal.mutex.acquire(); // Altera o estado do semaforo mutex para 1 (ocupado)
        Principal.state[id] = 1; // Altera o estado do filosofo para 1 (comendo)
        Principal.test(id); // Testa se o filosofo pode pegar os garfos
        Principal.mutex.release(); // Altera o estado do semaforo mutex para 0 (livre)
        Principal.s[id].acquire();  // Altera o estado do semaforo do filosofo para 1 (comendo)

        filosofoPane.styleProperty().set("-fx-background-image: url('filosofo" + id + "-comendo.png'); -fx-background-repeat: no-repeat;"); // Altera a imagem do filosofo para a imagem de comendo
      } else { // Caso a flag running esteja false (pausado)
        System.out.println("Filósofo " + id + " está parado de não pode pegar os garfos"); 
      } // Fim do if-else
    } catch (InterruptedException e) { // Tratamento de excecao caso a thread seja interrompida durante a execucao do metodo
      Thread.currentThread().interrupt(); // Interrompe a thread de fato
    } // Fim do try-catch
  } // Fim do metodo getForks

  /**
   * *************************************************************
   * Metodo: putForks
   * Funcao: altera o estado do semaforo mutex para 1 e testa se o filosofo pode devolver os garfos. Caso possa, altera o estado do semaforo para 0 e o filosofo tem seu proprio estado alterado para 0 (pensando)
   * Parametros: nao recebe parametros
   * Retorno: nao retorna valor
   *************************************************************** */
  public void putForks() throws InterruptedException {
    try {
      if (running) { // Testa se a flag running esta true (nao pausado)  
        Principal.mutex.acquire(); // Altera o estado do semaforo mutex para 1 (ocupado)
        Principal.state[id] = 0; // Altera o estado do filosofo para 0 (pensando)
        Principal.test(Principal.LEFT(id)); // Testa se o filosofo a esquerda pode pegar os garfos
        Principal.test(Principal.RIGHT(id)); // Testa se o filosofo a direita pode pegar os garfos
        Principal.mutex.release(); // Altera o estado do semaforo mutex para 0 (livre)

        filosofoPane.styleProperty().set("-fx-background-image: url('filosofo" + id + ".png'); -fx-background-repeat: no-repeat;"); // Altera a imagem do filosofo para a imagem padrao
      } else { // Caso a flag running esteja false (pausado)
        System.out.println("Filósofo " + id + " está parado e não pode devolver os garfos");
      } // Fim do if-else
    } catch (InterruptedException e) { // Tratamento de excecao caso a thread seja interrompida durante a execucao do metodo
      Thread.currentThread().interrupt(); // Interrompe a thread de fato
    } // Fim do try-catch
  } // Fim do metodo putForks

  // Metodos getters e setters dos atributos da classe
  public Pane getFilosofoPane() {
    return filosofoPane;
  }

  public void setFilosofoPane(Pane filosofoPane) {
    this.filosofoPane = filosofoPane;
  }

  public int getFilosofoId() {
    return id;
  }

  public void setFilosofoId(int id) {
    this.id = id;
  }

  public int getEatVelocity() {
    return eatVelocity;
  }

  public void setEatVelocity(int eatVelocity) {
    this.eatVelocity = eatVelocity;
  }

  public int getThinkVelocity() {
    return thinkVelocity;
  }

  public void setThinkVelocity(int thinkVelocity) {
    this.thinkVelocity = thinkVelocity;
  }

  public int getStateFilosofo() {
    return Principal.state[id];
  }

  public void setRunningFlag(boolean running) {
    this.running = running;
  }

  public boolean getRunningFlag() {
    return running;
  }
  // Fim dos metodos getters e setters
} // Fim da classe Filosofo3