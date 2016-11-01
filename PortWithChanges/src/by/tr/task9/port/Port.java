package by.tr.task9.port;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import by.tr.task9.ship.Ship;
import by.tr.task9.warehouse.Container;
import by.tr.task9.warehouse.Warehouse;

public class Port {
	private final static Logger logger = Logger.getRootLogger();

	private BlockingQueue<Berth> berthList; // очередь причалов
	private Warehouse portWarehouse; // хранилище порта
	
//TODO изменено
	private ConcurrentMap<Ship, Berth> usedBerths; // какой корабль у какого причала стоит

	public Port(int berthSize, int warehouseSize) {
		portWarehouse = new Warehouse(warehouseSize); // создаем пустое хранилище
		berthList = new ArrayBlockingQueue<Berth>(berthSize, true); // создаем очередь причалов
		for (int i = 0; i < berthSize; i++) { // заполняем очередь причалов непосредственно самими причалами
			berthList.add(new Berth(i, portWarehouse));
		}
//TODO изменено
		usedBerths = new ConcurrentHashMap<Ship, Berth>(); // создаем объект, который будет
		// хранить связь между кораблем и причалом
		logger.debug("Порт создан.");
	}

	public void setContainersToWarehouse(List<Container> containerList) {
		portWarehouse.addContainer(containerList);
	}

	public boolean lockBerth(Ship ship) {

		try {
			Berth berth = berthList.take();
			usedBerths.put(ship, berth);
			return true;
		} catch (InterruptedException e) {
			logger.debug("Кораблю " + ship.getName() + " отказано в швартовке.");
			return false;
		}
	}

	public boolean unlockBerth(Ship ship) {
		Berth berth = usedBerths.get(ship);

		try {
			berthList.put(berth);
			usedBerths.remove(ship);
			return true;
		} catch (InterruptedException e) {
			logger.debug("Корабль " + ship.getName() + " не смог отшвартоваться.");
			return false;
		}

	}

	public Berth getBerth(Ship ship) throws PortException {

		Berth berth = usedBerths.get(ship);
		if (berth == null) {
			throw new PortException("Try to use Berth without blocking.");
		}
		return berth;
	}
	//TODO добавлено
	public Warehouse getPortWarehouse() {
		return portWarehouse;
	}

}
