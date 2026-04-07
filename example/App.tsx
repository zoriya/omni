import type React from "react";
import { StyleSheet, View } from "react-native";
import { Omni } from "react-native-omni";

function App(): React.JSX.Element {
	return (
		<View style={styles.container}>
			<Omni isRed={true} style={styles.view} testID="omni" />
		</View>
	);
}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		justifyContent: "center",
		alignItems: "center",
	},
	view: {
		width: 200,
		height: 200,
	},
});

export default App;
