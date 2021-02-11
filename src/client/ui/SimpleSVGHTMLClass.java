package client.ui;

public class SimpleSVGHTMLClass {

    private String begin = "<html><head></head><body bgcolor='#F4F4F4'>";
    private String end = "</body></html>";
    private final String CONVIMGPATH = getClass().getResource("resources/icons/convbtn.svg").toExternalForm();
    private final String CONVPRESSEDIMGPATH = getClass().getResource("resources/icons/convbtnpressed.svg").toExternalForm();
    private final String ADDCONVIMGPATH = getClass().getResource("resources/icons/addconvbtn.svg").toExternalForm();
    private final String ADDCONVPRESSEDIMGPATH = getClass().getResource("resources/icons/addconvbtnpressed.svg").toExternalForm();
    private final String PARTICIPANTSIMGPATH = getClass().getResource("resources/icons/participantsbtn.svg").toExternalForm();
    private final String EDITIMGPATH = getClass().getResource("resources/icons/editbtn.svg").toExternalForm();
    private final String CONFIRMIMGPATH = getClass().getResource("resources/icons/confirmbtn.svg").toExternalForm();

    public String getStyleConfirm() {
        return styleConfirm;
    }

    private final String styleConfirm = getClass().getResource("resources/styles/body.css").toExternalForm();

    public enum IMAGE {
        ADD_CONV_BTN, CONV_BTN, ADD_CONV_BTN_PRESSED, CONV_BTN_PRESSED, PARTICIPANTBTN, EDITBTN, CONFIRMBTN
    }

    /**
     * @param obj which kind of image
     * @return complete html string for the svg graphic
     */
    public String getHTMLImage(IMAGE obj) {
        switch (obj) {
            case CONFIRMBTN:
                return begin + "<img style=\"cursor: pointer;\" title=\"Confirm\" src=\"" + CONFIRMIMGPATH + "\" height=\"20\" width=\"20\"/>" + end;
            case ADD_CONV_BTN:
                return begin + "<img style=\"cursor: pointer;\" title=\"Start Conversation\" src=\"" + ADDCONVIMGPATH + "\" height=\"55\" width=\"55\"/>" + end;
            case CONV_BTN:
                return begin + "<img style=\"cursor: pointer;\" title=\"Show Conversations\" src=\"" + CONVIMGPATH + "\" height=\"55\" width=\"55\"/>" + end;
            case CONV_BTN_PRESSED:
                return begin + "<img style=\"cursor: pointer;\" title=\"Show Conversations\" src=\"" + CONVPRESSEDIMGPATH + "\" height=\"55\" width=\"55\"/>" + end;
            case ADD_CONV_BTN_PRESSED:
                return begin + "<img style=\"cursor: pointer;\" title=\"Start Conversation\" src=\"" + ADDCONVPRESSEDIMGPATH + "\" height=\"55\" width=\"55\"/>" + end;
            case EDITBTN:
                return begin + "<img style=\"cursor: pointer;\" title=\"Configure Write Permissions\" src=\"" + EDITIMGPATH + "\" height=\"55\" width=\"55\" />" + end;
            case PARTICIPANTBTN:
                return begin + "<img style=\"cursor: pointer;\" title=\"Show Participants\" src=\"" + PARTICIPANTSIMGPATH + "\" height=\"55\" width=\"55\"/>" + end;
            default:
                break;
        }
        return null;
    }

}
