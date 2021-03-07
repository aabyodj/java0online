package m6t3.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class Tranciever {
	
	public static final int CHECKSUM = signatureToInt("CHCK");
	public static final int SEND_ALL = signatureToInt("BULK");
	public static final int SEND_STUDENT = signatureToInt("STUD");
	public static final int STOP = signatureToInt("STOP");
	public static final int SYNC_REQUEST = signatureToInt("SYNC");
	public static final long SYNC_INTERVAL = 3000;
	
	public static int signatureToInt(String str) {
		return ByteBuffer.wrap(str.getBytes()).getInt();
	}
	
	public static byte[] toBytes(int n) {
		byte[] result = new byte[Integer.BYTES];
		for (int i = 0; i < Integer.BYTES; i++) {
			result[i] = (byte) n;
			n = n >> 8;
		}
		return result;
	}
	
	public static byte[] toBytes(long n) {
		byte[] result = new byte[Long.BYTES];
		for (int i = 0; i < Long.BYTES; i++) {
			result[i] = (byte) n;
			n = n >> 8;
		}
		return result;
	}
	
	public static int getInt(byte[] bytes, int start) {
		int result = 0;
		int end = start + Integer.BYTES - 1;
		for (int i = end; i >= start; i--) {
			result = (result << 8) + bytes[i];
		}
		return result;
	}
	
	public static int getInt(byte[] bytes) {
		return getInt(bytes, 0);
	}
	
	public static long getLong(byte[] bytes, int start) {
		long result = 0;
		int end = start + Long.BYTES - 1;
		for (int i = end; i >= start; i--) {
			result = (result << 8) + bytes[i];
		}
		return result;
	}
	
	public static long getLong(byte[] bytes) {
		return getLong(bytes, 0);
	}
	
	public static byte[] recieveBytes(InputStream in, int len) throws IOException {
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++) {
			int x = in.read();
			if (x < 0) {
				in.close();
				throw new IOException("End of stream reached");
			} else {
				result[i] = (byte) x;
			}
		}
		return result;
	}
	
	public static int recieveInt(InputStream in) throws IOException {
		byte[] bytes = recieveBytes(in, Integer.BYTES);
		return getInt(bytes);
	}
	
	public static long recieveLong(InputStream in) throws IOException {
		byte[] bytes = recieveBytes(in, Long.BYTES);
		return getLong(bytes);
	}
	
	public static void transmitInt(int n, OutputStream out) throws IOException {
		out.write(toBytes(n));
	}	
	
	public static void transmitLong(long n, OutputStream out) throws IOException {
		out.write(toBytes(n));
	}
	
	public static Student recieveStudent(InputStream in) throws IOException {
		System.out.println("Приём студента");
		int len = recieveInt(in);
		byte[] buffer = recieveBytes(in, len);
		int pos = 0;
		int id = getInt(buffer, pos);
		pos += Integer.BYTES;
		int serial = getInt(buffer, pos);
		pos += Integer.BYTES;	
		int numLen = getInt(buffer, pos);
		pos += Integer.BYTES;	
		String number = new String(buffer, pos, numLen);
		pos += numLen; 
		int surLen = getInt(buffer, pos);
		pos += Integer.BYTES;	
		String surname = new String(buffer, pos, surLen);
		pos += surLen; 
		int nameLen = getInt(buffer, pos);
		pos += Integer.BYTES;	
		String name = new String(buffer, pos, nameLen);
		pos += nameLen; 
		int patLen = getInt(buffer, pos);
		pos += Integer.BYTES;	
		String patronymic = new String(buffer, pos, patLen);
		return new Student(id, serial, number, surname, name, patronymic);
	}
	
	public static void transmitStudent(Student student, OutputStream out) throws IOException {
		System.out.println("Отправка студента");
		byte[] signature = toBytes(SEND_STUDENT);
		byte[] number = student.getNumber().getBytes();
		byte[] surname = student.getSurname().getBytes();
		byte[] name = student.getName().getBytes();
		byte[] patronymic = student.getPatronymic().getBytes();	
		int len = (Integer.BYTES * (2 + 4))
				+ number.length + surname.length + name.length + patronymic.length;
		byte[] result = new byte[signature.length + Integer.BYTES + len];
		int i = 0;
		System.arraycopy(signature, 0, result, i, signature.length);
		i += signature.length;
		System.arraycopy(toBytes(len), 0, result, i, Integer.BYTES);
		i += Integer.BYTES;
		System.arraycopy(toBytes(student.id), 0, result, i, Integer.BYTES);
		i += Integer.BYTES;
		System.arraycopy(toBytes(student.getSerial()), 0, result, i, Integer.BYTES);
		i += Integer.BYTES;
		System.arraycopy(toBytes(number.length), 0, result, i, Integer.BYTES);
		i += Integer.BYTES;
		System.arraycopy(number, 0, result, i, number.length);
		i += number.length;
		System.arraycopy(toBytes(surname.length), 0, result, i, Integer.BYTES);
		i += Integer.BYTES;
		System.arraycopy(surname, 0, result, i, surname.length);
		i += surname.length;
		System.arraycopy(toBytes(name.length), 0, result, i, Integer.BYTES);
		i += Integer.BYTES;
		System.arraycopy(name, 0, result, i, name.length);
		i += name.length;
		System.arraycopy(toBytes(patronymic.length), 0, result, i, Integer.BYTES);
		i += Integer.BYTES;
		System.arraycopy(patronymic, 0, result, i, patronymic.length);
		out.write(result);
	}
	
	public static LinkedList<Student> recieveStudentsList(InputStream in) throws IOException {
		LinkedList<Student> result = new LinkedList<>();
		int signature = recieveInt(in);
		while (signature == SEND_STUDENT) {
			result.add(recieveStudent(in));
			signature = recieveInt(in);
		}
		if (signature != STOP) {
			in.close();
		}
		return result;
	}
}
