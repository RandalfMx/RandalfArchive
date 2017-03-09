/**
 * 
 */
package mx.randalf.archive.tools;

import java.io.File;
import java.io.FileFilter;

/**
 * @author massi
 *
 */
public class Folder {

	/**
	 * 
	 */
	public Folder() {
	}

	public static boolean deleteFolder(File path) {
		boolean ris = true;
		File[] fl = null;
		fl = path.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().equals(".") || pathname.getName().equals("..")) {
					return false;
				} else {
					return true;
				}
			}
		});
		for (int x = 0; x < fl.length; x++) {
			if (fl[x].isDirectory()) {
				if (!deleteFolder(fl[x])) {
					ris = false;
				}
			} else {
				if (!fl[x].delete()) {
					if (fl[x].exists()){
						ris = false;
					}
				}
			}
		}
		if (ris) {
			if (!path.delete()) {
				ris = false;
			}
		}
		return ris;
	}

}
