
package sr_splitter;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BidRecord {
    
    String fullText;
    Date clickDate;
    String userId;
    
}
