package by.tr.task9.ship;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import by.tr.task9.port.Berth;
import by.tr.task9.port.Port;
import by.tr.task9.port.PortException;
import by.tr.task9.warehouse.Container;
import by.tr.task9.warehouse.Warehouse;

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
		}
		catch (PortException e) {
			//TODO изменено сообщение
			logger.debug("Порт не смог обслужить корабль " + name + ". Возникли проблемы в работе порта.");
		}
	}

	private void atSea() throws InterruptedException {
		Thread.sleep(1000);
	}

	private void inPort() throws InterruptedException, PortException {

		boolean isLockedBerth = false;
		Berth berth = null;
		try {

			isLockedBerth = port.lockBerth(this);

			if (isLockedBerth) {

				berth = port.getBerth(this);
				logger.debug("Корабль " + name + " пришвартовался к причалу " + berth.getId() + " НА КОРАБЛЕ: "
						+ this.shipWarehouse.getRealSize());
				ShipAction action = getNextAction();
				executeAction(action, berth);

			} else {
				logger.debug("Кораблю " + name + " отказано в швартовке к причалу ");
			}
		} finally {
			if (isLockedBerth) {

				logger.debug("Корабль " + name + " ОТШВАРТОВАЛСЯ ОТ ПРИЧАЛА " + berth.getId());
				port.unlockBerth(this);
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
		//TODO изменен метод
		int containersNumberToMove = conteinersCount(this.shipWarehouse.getRealSize());
		boolean result = false;
		//TODO добавлено
		if (containersNumberToMove == 0) {
			logger.debug("На корабле " + name + " нет ни одного контейнера, он ничего не может выгрузить в порт.");
			return result;
		}
		logger.debug("Корабль " + name + " хочет загрузить " + containersNumberToMove + " контейнеров на склад порта.");

		result = berth.add(shipWarehouse, containersNumberToMove);

		if (!result) {
			logger.debug("Недостаточно места на складе порта для выгрузки кораблем " + name + " "
					+ containersNumberToMove + " контейнеров.");
		} else {
			logger.debug("Корабль " + name + " выгрузил " + containersNumberToMove
					+ " контейнеров в порт. ТЕПЕРЬ в порту " + berth.getPortWarehouse().getRealSize());
		}

		return result;
	}

	private boolean loadFromPort(Berth berth) throws InterruptedException {
		//TODO изменен метод
		int containersNumberToMove = conteinersCount(shipWarehouse.getFreeSize());

		boolean result = false;
		//TODO добавлено
		if (containersNumberToMove == 0) {
			logger.debug("Корабль " + name + " уже полностью заполнен, не может ничего загрузить из порта.");
			return result;
		}
		logger.debug(
				"Корабль " + name + " хочет загрузить " + containersNumberToMove + " контейнеров со склада порта.");

		result = berth.get(shipWarehouse, containersNumberToMove);

		if (result) {
			logger.debug("Корабль " + name + " загрузил " + containersNumberToMove
					+ " контейнеров из порта. ТЕПЕРЬ в порту " + berth.getPortWarehouse().getRealSize());
		} else {
			logger.debug("Недостаточно контейнеров в порту для погрузки " + containersNumberToMove + " контейнеров на "
					+ name);
		}

		return result;
	}
	
	//TODO изменено
	private int conteinersCount(int count) {
		int rand = new Random().nextInt(count + 1);
		while (rand == 0) {
			if (count != 0) {
				rand = new Random().nextInt(count + 1);
			} else {
				break;
			}
		}
		return rand;
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
