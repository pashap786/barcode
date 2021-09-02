package barcode;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import com.barcode.component.BarcodeInfo;
import com.barcode.decoder.BarcodeImageDecoder;

@RunWith(SpringRunner.class)
//@SpringBootTest
public class BarcodeTest {

	private Logger log = LoggerFactory.getLogger(BarcodeTest.class);
	
	private BarcodeImageDecoder bid = new BarcodeImageDecoder();
	
	@Test
	public void testBarcodeProcess() {
		Resource barcodefile = new ClassPathResource("samplejpg.jpg");
		try {
			InputStream is = barcodefile.getInputStream();
			BarcodeInfo bi = bid.decodeImage(is);
			log.info("{}", bi);
			assertTrue(bi != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
