/*******************************************************************************
*                                                                              *
*                               Сортировки. №3.                                *
*    Сортировка выбором. Дана последовательность чисел                         *
*  a 1 <= a 2 <= ... <= a n . Требуется переставить элементы так, чтобы они    *
*  были расположены по убыванию. Для этого в массиве, начиная с первого,       *
*  выбирается наибольший элемент и ставится на первое место, а первый - на     *
*  место наибольшего. Затем, начиная со второго, эта процедура повторяется.    *
*  Написать алгоритм сортировки выбором.                                       *
*------------------------------------------------------------------------------*
*    Так как по условию массив уже упорядочен по неубыванию, то достаточно     *
*  переставить все элементы задом наперёд. Но сортировка так сортировка.       *
*                                                                              *
*******************************************************************************/

import java.util.Scanner;


public class S3 {
  static Scanner in;


  public static void main(String[] args) {
    in = new Scanner(System.in);
    int n = in.nextInt();
    long[] a = new long[n];
    for (int i = 0; i < n; i++)
      a[i] = in.nextLong();

    for (int i = 0; i < n - 1; i++) {
      int max = i;
      for (int j = i + 1; j < n; j++)
        if (a[j] > a[max]) max = j;
      if (i != max) {
        long t = a[i]; a[i] = a[max]; a[max] = t;
      }
      System.out.print(a[i] + " ");
    }
    System.out.println(a[n - 1]);
  }
}