package net.mare.gtpatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class GrowtopiaPatcher {

	public static String usage = "Usage: java -jar GrowtopiaPatcher.jar <apk file> <new ip address> [alternative ip address]";

	public static void main(String[] args) {
		String filename = "";
		String ip = "";
		String altIp = "";
		if (args != null && args.length > 0 && args[0] != null) {
			filename = args[0];
			File apkfile = new File(filename);
			if (!(apkfile.exists() && !apkfile.isDirectory() && apkfile.canRead())) {
				System.err.println("Invalid file: " + filename);
				return;
			}
		} else {
			System.err.println(usage);
			return;
		}

		if (args.length < 2) {
			System.err.println(usage);
			return;
		}
		ip = args[1];
		if (args.length < 3) {
			altIp = ip;
		} else if (args.length == 3) {
			altIp = args[2];
		}
		try {
			replace(filename, ip, altIp);
		} catch (Exception e) {
			System.err.println("Something went wrong. Please submit a report with the details below: ");
			e.printStackTrace();
			return;
		}
	}

	public static void replace(String filename, String ip, String altIp) throws Exception {

		ZipFile apk = new ZipFile(filename);
		ZipOutputStream zos = new ZipOutputStream(
				new FileOutputStream(filename.substring(0, filename.lastI	ndexOf(".")) + "_patched.apk"));
		for (Enumeration e = apk.entries(); e.hasMoreElements();) {
			ZipEntry entryIn = (ZipEntry) e.nextElement();
			
			ZipEntry entr = new ZipEntry(entryIn.getName());
			entr.setCrc(entryIn.getCrc());
			entr.setSize(entryIn.getSize());
			entr.setMethod(entryIn.getMethod());
			if (entryIn.getName().contains("libgrowtopia.so")) {
				zos.putNextEntry(entr);
				File tmp = new File("tmp_libgrowtopia.so");
				tmp.deleteOnExit();
				FileOutputStream fos = new FileOutputStream(tmp);
				InputStream is = apk.getInputStream(entryIn);
				byte[] buf = new byte[1024];
				int len;
				while ((len = is.read(buf)) > 0) {
					fos.write(buf, 0, len);
				}
				fos.close();
				

				RandomAccessFile f = new RandomAccessFile("tmp_libgrowtopia.so", "r");
				byte[] b = new byte[(int) f.length()];
				f.readFully(b);
				f.close();
				byte[] toreplace = "growtopia1.com".getBytes();
				byte[] replacer = ip.getBytes();
				for (int i = 0; i < b.length - toreplace.length; i++) {
					boolean success = true;
					for (int j = 0; j < toreplace.length; j++) { //find location of growtopia1.com
						if (b[i + j] != toreplace[j]) {
							success = false; //growtopia1.com found
							break;
						}
					}
					if (success) { //check if growtopia1.com was found.
						for (int j = 0; j < toreplace.length; j++) {
							if (j < replacer.length) {
								b[i + j] = replacer[j]; //replace growtopia1.com with some other ip.
							} else {
								b[i + j] = 0; //pad with zeros if necessary.
							}
						}
					}
				}

				toreplace = "growtopia2.com".getBytes();
				replacer = altIp.getBytes();
				for (int i = 0; i < b.length - toreplace.length; i++) {
					boolean success = true;
					for (int j = 0; j < toreplace.length; j++) { //find location of growtopia2.com
						if (b[i + j] != toreplace[j]) {
							success = false; //growtopia1.com found
							break;
						}
					}
					if (success) { //check if growtopia2.com was found
						for (int j = 0; j < toreplace.length; j++) {
							if (j < replacer.length) {
								b[i + j] = replacer[j]; //replace growtopia1.com with some other ip.
							} else {
								b[i + j] = 0; //pad with zeros if necessary.
							}
						}
					}
				}
				zos.write(b);
				
			} else {
				zos.putNextEntry(entr);
				InputStream is = apk.getInputStream(entryIn);
				byte[] buf = new byte[1024];
				int len;
				while ((len = is.read(buf)) > 0) {
					zos.write(buf, 0, len);
				}
			}
			zos.closeEntry();
		}
		zos.close();
		apk.close();
		System.out.println(
				"Success! Now it's up to you to sign the apk (look up apk-signer from Google Play Store for example)");
	}
}
