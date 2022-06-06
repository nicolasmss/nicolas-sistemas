/* 
   Implementacao do Jantar dos Filosofos com Semaforo
   PUCRS - Escola Politecnica
   Prof: Fernando Dotti
*/
import java.util.Scanner;
import java.util.concurrent.Semaphore;

// ============= Filosofo ==============
class Barreira{
	int contador=0;
	int total=6;
	PessoasBarradas []pessoas = new PessoasBarradas[total];

	public void popular(Semaphore mutex,Semaphore barreira){
		for (int i=0;i<total;i++){
			pessoas[i] = new PessoasBarradas(i,mutex,barreira);
		}
	}

	public void rodar(){
		for (int i =0;i<total;i++){
			pessoas[i].start();
		}
	}

	class PessoasBarradas extends Thread {
		private int i;
		private Semaphore mutex, barreira;

		public PessoasBarradas(int _i, Semaphore _g1, Semaphore _g2){
			i = _i;
			mutex = _g1;
			barreira = _g2;
		}

		public void run() {
			Scanner in = new Scanner(System.in);
			while (true) {
				try {
					mutex.acquire();
				} catch (InterruptedException e) {}
				contador++;
				mutex.release();
				System.out.println("opa"+i);
				try {
					mutex.acquire();
				} catch (InterruptedException e) {}
				if (contador==total){
					barreira.release();
				}
				mutex.release();
				try {
					barreira.acquire();
				} catch (InterruptedException e) {}
				barreira.release();
				System.out.println("ai sim"+i);
				in.nextLine();
			}
		}
	}
}


class BarreiraRoda {
	public static void main(String[] args) {
		Barreira barreiras = new Barreira();
		Semaphore mutex = new Semaphore(1);
		Semaphore barreira = new Semaphore(0);

		barreiras.popular(mutex,barreira);
		barreiras.rodar();



	}	  
}
	
