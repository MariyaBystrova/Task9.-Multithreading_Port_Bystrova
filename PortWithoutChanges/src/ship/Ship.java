package ship;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import port.Berth;
import port.Port;
import port.PortException;
import warehouse.Container;
import warehouse.Warehouse;

public class Ship implements Runnable {

	private final static Logger logger = Logger.getRootLogger();
	private volatile boolean stopThread = false;

	private String name;
	private Port port;
	private Warehouse shipWarehouse;

	public Ship(String name, Port port, int shipWarehouseSize) {
		this.name = name;
		this.port = port;
		shipWarehouse = new Warehouse(shipWarehouseSize);
	}

	public void setContainersToWarehouse(List<Container> containerList) {
		shipWarehouse.addContainer(containerList);
	}

	public String getName() {
		return name;
	}

	public void stopThread() {
		stopThread = true;
	}

	public void run() {
		try {
			while (!stopThread) {
				atSea();
				inPort();
			}
		} catch (InterruptedException e) {
			logger.error("С кораблем случилась неприятность и он уничтожен.", e);
		} catch (PortException e) {
//TODO скоррекктировано сообщение
			logger.error("С кораблем случилась неприятность и он уничтожен.", e);//!!! переписать сообщение
		}
	}

	private void atSea() throws InterruptedException {
		Thread.sleep(1000);
	}


	private void inPort() throws PortException, InterruptedException {

		boolean isLockedBerth = false;
		Berth berth = null;
		try {
			isLockedBerth = port.lockBerth(this);
			
			if (isLockedBerth) {
				berth = port.getBerth(this);
				logger.debug("Корабль " + name + " пришвартовался к причалу " + berth.getId());
				ShipAction action = getNextAction();
				executeAction(action, berth);
			} else {
				logger.debug("Кораблю " + name + " отказано в швартовке к причалу ");
			}
		} finally {
			if (isLockedBerth){
				port.unlockBerth(this);
				logger.debug("Корабль " + name + " отошел от причала " + berth.getId());
			}
		}
		
	}

	private void executeAction(ShipAction action, Berth berth) throws InterruptedException {
		switch (action) {
		case LOAD_TO_PORT:
 				loadToPort(berth);
			break;
		case LOAD_FROM_PORT:
				loadFromPort(berth);
			break;
		}
	}

	private boolean loadToPort(Berth berth) throws InterruptedException {
//TODO метод conteinersCount(this.shipWarehouse.getRealSize()) исходя из измененной логики метода
		int containersNumberToMove = conteinersCount();
		boolean result = false;
//TODO  исходя из добавленной логики методу conteinersCount, необходимо обработать тот случай, 
// 		 когда на корабле нет ни одного контейнера, т е он не может ничего выгрузить
		/*if(containersNumberToMove == 0){
			logger.debug(
					"На корабле " + name + " нет ни одного контейнера, он ничего не может выгрузить в порт.");
			return result;
		}*/
		
		logger.debug("Корабль " + name + " хочет загрузить " + containersNumberToMove
				+ " контейнеров на склад порта.");

		result = berth.add(shipWarehouse, containersNumberToMove);
		
		if (!result) {
			logger.debug("Недостаточно места на складе порта для выгрузки кораблем "
					+ name + " " + containersNumberToMove + " контейнеров.");
		} else {
			logger.debug("Корабль " + name + " выгрузил " + containersNumberToMove
					+ " контейнеров в порт.");
			
		}
		return result;
	}

	private boolean loadFromPort(Berth berth) throws InterruptedException {
//TODO метод conteinersCount(shipWarehouse.getFreeSize()) исходя из измененной логики метода
// (не может загрузить себе больше, чем у него свободного места)
		int containersNumberToMove = conteinersCount();
		
		boolean result = false;

//TODO  исходя из добавленной логики методу conteinersCount, необходимо обработать тот случай, 
//		 когда корабль уже полностью заполнен и не может ничего загрузить из порта		
		/*if(containersNumberToMove == 0){
			logger.debug(
					"Корабль " + name + " уже полностью заполнен, не может ничего загрузить из порта.");
			return result;
		}*/
		
		
		logger.debug("Корабль " + name + " хочет загрузить " + containersNumberToMove
				+ " контейнеров со склада порта.");
		
		result = berth.get(shipWarehouse, containersNumberToMove);
		
		if (result) {
			logger.debug("Корабль " + name + " загрузил " + containersNumberToMove
					+ " контейнеров из порта.");
		} else {
			logger.debug("Недостаточно места на на корабле " + name
					+ " для погрузки " + containersNumberToMove + " контейнеров из порта.");
		}
		
		return result;
	}

	//TODO переписать метод следующим образом, чтобы могло генерироваться только 
	//     < кол-ва, что может загрузить корабль при загрузке из порта
	//	   < кол-ва, что может выгрузить корабль при выгрузке в порт
	//  + добавлена возможность генерации значения 0 только в случае count==0
	// Использовано т.к. считается нелогичным, что корабль запрашивает больше на выгрузку/погрузку, чем сам способен обработать
	
	/*private int conteinersCount(int count) {
		int rand = new Random().nextInt(count + 1);
		while (rand == 0) {
			if (count != 0) {
				rand = new Random().nextInt(count + 1);
			} else {
				break;
			}
		}
		return rand;
	}*/
	
	private int conteinersCount() {
		Random random = new Random();
		return random.nextInt(20) + 1;
	}

	private ShipAction getNextAction() {
		Random random = new Random();
		int value = random.nextInt(4000);
		if (value < 1000) {
			return ShipAction.LOAD_TO_PORT;
		} else if (value < 2000) {
			return ShipAction.LOAD_FROM_PORT;
		}
		return ShipAction.LOAD_TO_PORT;
	}

	enum ShipAction {
		LOAD_TO_PORT, LOAD_FROM_PORT
	}
}
