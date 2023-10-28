import javafx.scene.layout.Pane;

public class Filosofo extends Thread {
  // Atributos
  private Pane filosofoPane;
  private int id;
  private int thinkVelocity;
  private int eatVelocity;
  private volatile boolean running = true;

  public void setRunningFlag(boolean running) {
    this.running = running;
  }

  public boolean getRunningFlag() {
    return running;
  }

  // Construtor
  public Filosofo(Pane filosofoPane, int id) {
    this.filosofoPane = filosofoPane;
    this.id = id;
  }

  // Metodos
  @Override
  public void run() {
    while (true) {

      while (running) {
        think();
        try {
          getForks();
          eat();
          putForks();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }

      if (Thread.currentThread().isInterrupted()) {
        break;
      }

    }
  }

  public void pauseIt() {
    running = false;
  }

  public void resumeIt() {
    running = true;
  }

  public void think() {
    long time = getThinkVelocity();
    //filosofoPane.styleProperty().set("-fx-background-image: url('filosofo" + id + ".png'); -fx-background-repeat: no-repeat;");
    //filosofoPane.styleProperty().set("-fx-background-image: url('filosofo" + id + "-pensando.png'); -fx-background-repeat: no-repeat;");
    
    try {
      System.out.println("Filósofo " + id + " está pensando por " + time + " segundos.");
      Thread.sleep(time*1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void eat() {
    long time = getEatVelocity();
    //filosofoPane.styleProperty().set("-fx-background-image: url('filosofo" + id + "-comendo.png'); -fx-background-repeat: no-repeat;");
    
    try {
      System.out.println("Filósofo " + id + " está comendo por " + time + " segundos.");
      Thread.sleep(time*1000);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void getForks() {
    if (running) {  
      try {
        Principal.mutex.acquire();
        Principal.state[id] = 1;
        Principal.test(id);
        Principal.mutex.release();
        Principal.s[id].acquire();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      //System.out.println("Filósofo " + id + " pegou o garfo esquerdo.");
      //System.out.println("Filósofo " + id + " pegou o garfo direito.");

      filosofoPane.styleProperty().set("-fx-background-image: url('filosofo" + id + "-comendo.png'); -fx-background-repeat: no-repeat;");
    } else {
      System.out.println("Filósofo " + id + " está parado de não pode pegar os garfos");
    }
  }

  public void putForks() throws InterruptedException {
    if (running) {  
      Principal.mutex.acquire();
      Principal.state[id] = 0;
      Principal.test(Principal.LEFT(id));
      Principal.test(Principal.RIGHT(id));
      Principal.mutex.release();

      //System.out.println("Filósofo " + id + " soltou o garfo esquerdo.");
      //System.out.println("Filósofo " + id + " soltou o garfo direito.");

      filosofoPane.styleProperty().set("-fx-background-image: url('filosofo" + id + ".png'); -fx-background-repeat: no-repeat;");
      //filosofoPane.styleProperty().set("-fx-background-image: url('filosofo" + id + "-pensando.png'); -fx-background-repeat: no-repeat;");
    } else {
      System.out.println("Filósofo " + id + " está parado e não pode devolver os garfos");
    }
  }

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
}