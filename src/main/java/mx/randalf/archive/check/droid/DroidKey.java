package mx.randalf.archive.check.droid;

public enum DroidKey {
	
	ID(0), 					// * 2
	PARENT_ID(1), 			// * 0
	URI(2), 				// file:/mnt/volume1/Temporaneo/Tar/dc753296-dde4-4a5d-849d-a5e1c96d25e4/CFI0882858.tar
	FILE_PATH(3), 			// /mnt/volume1/Temporaneo/Tar/dc753296-dde4-4a5d-849d-a5e1c96d25e4/CFI0882858.tar
	NAME(4), 				// * CFI0882858.tar
	METHOD(5), 				// Signature
	STATUS(6),				// Done
	SIZE(7),				// * 310927360
	TYPE(8),				// * Container
	EXT(9),					// * tar
	LAST_MODIFIED(10),		// * 2016-11-14T23:32:52
	EXTENSION_MISMATCH(11),	// false
	HASH(12),				// 
	FORMAT_COUNT(13),		// 1
	PUID(14),				// * x-fmt/265
	MIME_TYPE(15),			// * application/x-tar
	FORMAT_NAME(16),		// * Tape Archive Format
	FORMAT_VERSION(17);		// * 

	private final Integer value;

	DroidKey(Integer value){
		this.value = value;
	}

	public int value(){
		return this.value;
	}

    public static DroidKey fromValue(Integer v) {
        for (DroidKey c: DroidKey.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(""+v);
    }
}
