package com.kleegroup.stages.datavizneo4j.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

public class Utilities {

	/**
	 * Permet de calculer la moyenne de Double dans une liste.
	 * 
	 * @param list Liste de Double pour laquelle la moyenne va être calculer
	 * @return La moyenne des Double dans la liste
	 */
	public static int CalculateAverageInteger(List<Integer> values1) {
		Assertion.checkNotNull(values1);
		int sum = 0;
		if (!values1.isEmpty()) {
			for (Integer mark : values1) {
				sum += mark;
			}
			return sum / values1.size();
		}
		return sum;
	}

	/**
	 * Permet de calculer la moyenne de Double dans une liste.
	 * 
	 * @param list Liste de Double pour laquelle la moyenne va être calculer
	 * @return La moyenne des Double dans la liste
	 */
	public static double CalculateAverageDouble(List<Double> list) {
		Assertion.checkNotNull(list);
		Double sum = 0.0;
		if (!list.isEmpty()) {
			for (Double mark : list) {
				sum += mark;
			}
			return Math.round(sum.doubleValue() / list.size() * 100.0) / 100.0;
		}
		return sum;
	}

	/**
	 * Get a key in a map given its corresponding value (value must be unique)
	 * 
	 * @param map 
	 * @param value
	 * @return
	 */
	public static Object getKeyFromValue(Map<Integer, Integer> map, Object value) {
		Assertion.checkNotNull(map);
		Assertion.checkNotNull(value);
		for (Object o : map.keySet()) {
			if (map.get(o).equals(value)) {
				return o;
			}
		}
		return null;
	}

	/**
	 * Delete all files in a folder and itself
	 * 
	 * @param folder the folder to delete
	 */
	public static void DeleteFolder(File folder) {
		Assertion.checkNotNull(folder);
		File[] files = folder.listFiles();
		if (files != null) { //some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					DeleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	/**
	 * Envoie d'un fichier vers un serveur distant via SFTP
	 * @param filePath Chemin d'accès vers le fichier en local
	 */
	public static void sftpscript(String filePath) {
		Assertion.checkNotNull(filePath);
		try {
			String user = "oayoub"; // username for remote host
			String password = "dataviz-neo4j"; // password of the remote host
			String host = "172.20.85.200"; // remote host address
			int port = 22;
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);
			session.connect();

			ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
			sftpChannel.connect();
			String name = filePath.substring(filePath.lastIndexOf("/") + 1);
			System.out.println(name);
			sftpChannel.put(filePath, name);
			sftpChannel.disconnect();
			session.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Télécharge un fichier depuis un URL vers un chemin d'accès.
	 * @param	url		URL de téléchargement
	 * @param	path	Chemin d'accès vers le fichier à créer
	 */
	public static void downloadfile(URL url, String path) {
		Assertion.checkNotNull(url);
		Assertion.checkNotNull(path);
		try (BufferedInputStream in = new BufferedInputStream(url.openStream())) {
			FileOutputStream fout = new FileOutputStream(path);
			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
			fout.close();
		} catch (IOException e) {
			throw WrappedException.wrap(e);
		}
	}
}
