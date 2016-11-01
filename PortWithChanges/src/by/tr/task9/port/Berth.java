package by.tr.task9.port;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import by.tr.task9.warehouse.Container;
import by.tr.task9.warehouse.Warehouse;

public class Berth {

	private int id;
	private Warehouse portWarehouse;

	public Berth(int id, Warehouse warehouse) {
		this.id = id;
		portWarehouse = warehouse;
	}

	public int getId() {
		return id;
	}

	public boolean add(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;
		Lock portWarehouseLock = portWarehouse.getLock();
		boolean portLock = false;

		try {
			portLock = portWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (portLock) {
				int newConteinerCount = portWarehouse.getRealSize() + numberOfConteiners;
				//TODO изменено
				if (newConteinerCount <= portWarehouse.getSize()) {
					result = doMoveFromShip(shipWarehouse, numberOfConteiners);
				}
			}
		} finally {
			if (portLock) {
				portWarehouseLock.unlock();
			}
		}

		return result;
	}

	private boolean doMoveFromShip(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		Lock shipWarehouseLock = shipWarehouse.getLock();
		boolean shipLock = false;

		try {
			shipLock = shipWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (shipLock) {
				if (shipWarehouse.getRealSize() >= numberOfConteiners) {
					List<Container> containers = shipWarehouse.getContainer(numberOfConteiners);
					portWarehouse.addContainer(containers);
					return true;
				}
			}
		} finally {
			if (shipLock) {
				shipWarehouseLock.unlock();
			}
		}

		return false;
	}

	public boolean get(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;
		Lock portWarehouseLock = portWarehouse.getLock();
		boolean portLock = false;

		try {
			portLock = portWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (portLock) {
				if (numberOfConteiners <= portWarehouse.getRealSize()) {
					result = doMoveFromPort(shipWarehouse, numberOfConteiners);
				}
			}
		} finally {
			if (portLock) {
				portWarehouseLock.unlock();
			}
		}

		return result;
	}

	private boolean doMoveFromPort(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		Lock shipWarehouseLock = shipWarehouse.getLock();
		boolean shipLock = false;

		try {
			shipLock = shipWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (shipLock) {
				//TODO изменено
				if (shipWarehouse.getFreeSize() >= numberOfConteiners) {
					List<Container> containers = portWarehouse.getContainer(numberOfConteiners);
					shipWarehouse.addContainer(containers);
					return true;
				}
			}
		} finally {
			if (shipLock) {
				shipWarehouseLock.unlock();
			}
		}

		return false;
	}

	//TODO добавлено
	public Warehouse getPortWarehouse() {
		return portWarehouse;
	}

}
