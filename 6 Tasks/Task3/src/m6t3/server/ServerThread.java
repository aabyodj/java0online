package m6t3.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

import m6t3.common.Student;

class ServerThread extends Thread {
	List<Student> students = new LinkedList<>();
	int nextId = 0;
	final ServerSocket socket;
	final List<SrvLink> links = new LinkedList<>();
	volatile boolean running = true;
	
	ServerThread(int port) throws IOException {
		socket = new ServerSocket(port);
	}

	@Override
	public void run() {
		try {
			while (running) {
				links.add(new SrvLink(this, socket.accept()));
			}
		} catch (Exception e) {
			if (!running) {
				e.printStackTrace();
			}
		}
		halt();
	}

	public void halt() {
		running = false;
		while (!links.isEmpty()) {
			links.remove(0).close();
		}
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	void updateStudent(Student upd) {
		synchronized (students) {
			if (upd.id < 0) {
				if (upd.getSerial() >= 0) {
					Student student = new Student(nextId++, upd);
					if (students.add(student)) {
						broadcast(student);
					}
				}
				return;
			}
			for (var student: students) {
				if (student.id == upd.id) {
					if (upd.getSerial() < 0) {
						students.remove(student);
						broadcast(upd);
					} else {
						upd.incSerial();
						if (student.update(upd)) {
							broadcast(student);
						}
					}
					return;
				}
			}
		}
	}

	private void broadcast(Student student) {
		for (var link: links) {
			try {
				link.outQueue.put(student);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
}
